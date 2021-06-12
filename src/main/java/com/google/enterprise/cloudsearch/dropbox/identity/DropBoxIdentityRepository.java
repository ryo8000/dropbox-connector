/*
 * Copyright 2021 Ryo H
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.enterprise.cloudsearch.dropbox.identity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.team.GroupMemberInfo;
import com.dropbox.core.v2.team.MemberProfile;
import com.dropbox.core.v2.team.TeamMemberInfo;
import com.dropbox.core.v2.teamcommon.GroupSummary;
import com.google.api.services.cloudidentity.v1.model.EntityKey;
import com.google.api.services.cloudidentity.v1.model.Membership;
import com.google.api.services.cloudidentity.v1.model.MembershipRole;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.enterprise.cloudsearch.dropbox.client.DropBoxClientFactory;
import com.google.enterprise.cloudsearch.dropbox.client.TeamClient;
import com.google.enterprise.cloudsearch.dropbox.model.DropBoxConfiguration;
import com.google.enterprise.cloudsearch.sdk.CheckpointCloseableIterable;
import com.google.enterprise.cloudsearch.sdk.CheckpointCloseableIterableImpl;
import com.google.enterprise.cloudsearch.sdk.identity.IdentityGroup;
import com.google.enterprise.cloudsearch.sdk.identity.IdentityUser;
import com.google.enterprise.cloudsearch.sdk.identity.Repository;
import com.google.enterprise.cloudsearch.sdk.identity.RepositoryContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** An Identity repository for listing Users and Groups form a repository. */
final class DropBoxIdentityRepository implements Repository {
  /** Log output */
  private static final Logger log = Logger.getLogger(DropBoxIdentityRepository.class.getName());
  /** Membership roles */
  private static final ImmutableList<MembershipRole> MEMBER_ROLES =
      ImmutableList.of(new MembershipRole().setName("MEMBER"));

  /** Injected context, provides convenience methods for building users & groups */
  private RepositoryContext repositoryContext;
  /** {@inheritDoc} */
  private TeamClient teamClient;

  DropBoxIdentityRepository() {
  }

  /**
   * Initializes the repository once the SDK is initialized.
   *
   * @param context injected context, contains convenience methods for building users & groups.
   * @throws IOException if unable to initialize.
   */
  @Override
  public void init(RepositoryContext context) throws IOException {
    repositoryContext = checkNotNull(context, "repository context can not be null");
    DropBoxConfiguration dropBoxConfiguration = DropBoxConfiguration.fromConfiguration();
    teamClient = DropBoxClientFactory.getTeamClient(dropBoxConfiguration.getCredentialFile());
  }

  /**
   * Retrieves all user identity mappings for the identity source.
   *
   * @param checkpoint saved state if paging over large result sets.
   * @return iterator of user identity mappings.
   * @throws IOException if unable to read user identity mappings.
   */
  @Override
  public CheckpointCloseableIterable<IdentityUser> listUsers(byte[] checkpoint) throws IOException {
    List<TeamMemberInfo> members;
    try {
      members = teamClient.getMembers();
    } catch (DbxException e) {
      throw new IOException("Failed to get members", e);
    }

    List<IdentityUser> identityUsers = members
        .stream()
        .map(user -> convertToIdentityUser(user))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    return new CheckpointCloseableIterableImpl.Builder<>(identityUsers).build();
  }

  /**
   * Retrieves all group rosters for the identity source.
   *
   * @param checkpoint saved state if paging over large result sets.
   * @return iterator of group rosters.
   * @throws IOException if unable to read groups.
   */
  @Override
  public CheckpointCloseableIterable<IdentityGroup> listGroups(byte[] checkpoint)
      throws IOException {
    List<GroupSummary> groups;
    try {
      groups = teamClient.getGroups();
    } catch (DbxException e) {
      throw new IOException("Failed to get groups", e);
    }

    List<IdentityGroup> identityGroups = new ArrayList<>();
    for (GroupSummary group : groups) {
      identityGroups.add(convertToIdentityGroup(group));
    }
    return new CheckpointCloseableIterableImpl.Builder<>(identityGroups).build();
  }

  /**
   * Closes the data repository and releases resources sch as connections or executors.
   */
  @Override
  public void close() {
  }

  /**
   * Convert a DropBox user to an identity user. If it is not possible, return {@code null}.
   *
   * @param user a DropBox user.
   * @return an identity user or {@code null}.
   */
  private IdentityUser convertToIdentityUser(TeamMemberInfo user) {
    MemberProfile profile = user.getProfile();
    if (!isValidMember(profile)) {
      log.log(Level.WARNING, "Skipping invalid User: {0}.", user);
      return null;
    }

    String googleId = profile.getEmail();
    String externalId = profile.getTeamMemberId();
    return repositoryContext.buildIdentityUser(googleId, externalId);
  }

  /**
   * Convert a DropBox group to an identity group.
   *
   * @param group a DropBox group.
   * @return an identity group.
   * @throws IOException if unable to convert to an identity group.
   */
  private IdentityGroup convertToIdentityGroup(GroupSummary group) throws IOException {
    List<GroupMemberInfo> groupMembers = listGroupMembers(group.getGroupId());

    List<GroupMemberInfo> filteredGroupMembers = groupMembers
        .stream()
        .filter(groupMember -> isValidMember(groupMember.getProfile()))
        .collect(Collectors.toList());
    Supplier<Set<Membership>> memberships = new MembershipsSupplier(filteredGroupMembers);
    return repositoryContext.buildIdentityGroup(group.getGroupName(), memberships);
  }

  /**
   * Retrieves all group members for the identity source.
   *
   * @param groupId a DropBox group ID.
   * @return group members.
   * @throws IOException if unable to retrieve group members.
   */
  private List<GroupMemberInfo> listGroupMembers(String groupId) throws IOException {
    List<GroupMemberInfo> groupMembers;
    try {
      groupMembers = teamClient.getGroupMembers(groupId);
    } catch (DbxException e) {
      throw new IOException("Failed to get group members", e);
    }
    return groupMembers;
  }

  /**
   * Validate member.
   *
   * @param profile member profile.
   * @return {@code true} if the data format is correct.
   */
  private boolean isValidMember(MemberProfile profile) {
    return !(Strings.isNullOrEmpty(profile.getEmail())
        || Strings.isNullOrEmpty(profile.getTeamMemberId()));
  }

  /** Provides group memberships based on DropBox group members information. */
  private class MembershipsSupplier implements Supplier<Set<Membership>> {
    /** List of DropBox members */
    private final List<GroupMemberInfo> groupMembers;

    /**
     * Get an instance of {@link MembershipsSupplier}.
     *
     * @param groupMembers list of DropBox members.
     */
    MembershipsSupplier(List<GroupMemberInfo> groupMembers) {
      this.groupMembers = groupMembers;
    }

    /**
     * Provides a set of memberships based on DropBox members information.
     *
     * @return a set of memberships.
     */
    @Override
    public Set<Membership> get() {
      Set<Membership> members = new HashSet<>();
      for (GroupMemberInfo groupMember : groupMembers) {
        EntityKey key = new EntityKey().setId(groupMember.getProfile().getEmail());

        Membership membership = new Membership()
            .setPreferredMemberKey(key)
            .setRoles(MEMBER_ROLES);
        members.add(membership);
      }
      return members;
    }
  }
}
