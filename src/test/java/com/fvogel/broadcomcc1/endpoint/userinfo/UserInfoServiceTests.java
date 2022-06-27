/*
 * Copyright 2022 [CopyrightOwner]
 */

package com.fvogel.broadcomcc1.endpoint.userinfo;

import com.fvogel.broadcomcc1.math.SecureRandomSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests of {@link UserInfoService }
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unused"})
class UserInfoServiceTests {

	@InjectMocks
	private UserInfoService userInfoService;

	@Mock
	private UserInfoRepository userInfoRepository;

	@Mock
	private SecureRandomSeries mockRandomSeries;

	final SecureRandomSeries randomSeries = new SecureRandomSeries();

	@Spy
	private final UserInfoResourceToBeanConverter userInfoResourceToBeanConverter = new UserInfoResourceToBeanConverter();

	@Spy
	private final UserInfoBeanToResourceConverter userInfoBeanToResourceConverter = new UserInfoBeanToResourceConverter();

	@Spy
	private final ConversionService conversionService = FakeConversionService.build();

	private List<UserInfoEntityBean> userInfoList;

	@BeforeEach
	void setUpEachTime() {
		UserInfoEntityBean item1 = new UserInfoEntityBean(1L, randomSeries.nextResourceId(), "text 1");
		UserInfoEntityBean item2 = new UserInfoEntityBean(2L, randomSeries.nextResourceId(), "text 2");
		UserInfoEntityBean item3 = new UserInfoEntityBean(3L, randomSeries.nextResourceId(), "text 3");

		userInfoList = new LinkedList<>();
		userInfoList.add(item1);
		userInfoList.add(item2);
		userInfoList.add(item3);
	}

	/**
	 * Unit tests of the findAll method
	 */
	@Nested
	class FindAllTests {

		@Test
		void shouldReturnAllRowsWhenRepositoryIsNotEmpty() {
			given(userInfoRepository.findAll()).willReturn(userInfoList);

			List<UserInfo> result = userInfoService.findAllUserInfos();

			then(result).isNotNull(); // must never return null
			then(result.size()).isEqualTo(userInfoList.size()); // must return all rows
		}

		@Test
		void shouldReturnEmptyListWhenRepositoryIsEmpty() {
			given(userInfoRepository.findAll()).willReturn(new ArrayList<>());

			List<UserInfo> result = userInfoService.findAllUserInfos();

			then(result).isNotNull(); // must never get null back
			then(result.size()).isZero(); // must have no content for this edge case
		}
	}

	@Nested
	class FindByTextTests {
		/*
		 * Happy path - some rows match the given text
		 */
		@Test
		void shouldReturnRowsWhenRowsWithTextExists() {
			// given
			Pageable pageable = PageRequest.of(1, 20);
			int start = (int) pageable.getOffset();
			int end = Math.min((start + pageable.getPageSize()), userInfoList.size());
			Page<UserInfoEntityBean> page = new PageImpl<>(userInfoList, pageable, userInfoList.size());

			// we're not validating what text gets passed to the repo, only that a result
			// set comes back
			given(userInfoRepository.findByText(any(), any(Pageable.class))).willReturn(page);

			// when/then
			List<UserInfo> result = userInfoService.findByText("text", 1, 20);

			then(result).isNotNull(); // must never return null

			// depending on which is smaller, the sample size or the page size, we expect
			// that many rows back
			then(result.size()).isEqualTo(Math.min(userInfoList.size(), pageable.getPageSize()));
		}

		@Test
		void shouldReturnEmptyListWhenNoDataFound() {
			given(userInfoRepository.findByText(any(), any(Pageable.class))).willReturn(Page.empty());

			List<UserInfo> result = userInfoService.findByText("foo", 1, 100);

			then(result).isNotNull(); // must never get null back
			then(result.size()).isZero(); // must have no content for this edge case
		}

		@Test
		void shouldThrowNullPointerExceptionWhenUserInfoIsNull() {
			assertThrows(NullPointerException.class, () -> userInfoService.findByText(null, 1, 100));
		}
	}

	@Nested
	class FindUserInfoByResourceIdTests {
		/*
		 * Happy path - finds the entity in the database
		 */
		@Test
		void shouldReturnOneWhenRepositoryContainsMatch() {
			// given
			String expectedId = randomSeries.nextResourceId();
			Optional<UserInfoEntityBean> expected = Optional.of(new UserInfoEntityBean(1L, expectedId, "sample"));
			given(userInfoRepository.findByResourceId(any())).willReturn(expected);

			// when/then
			Optional<UserInfo> actual = userInfoService.findUserInfoByResourceId(expectedId);

			assertThat(actual).isNotNull().isPresent();
			assertThat(actual.get().getResourceId()).isEqualTo(expectedId);
		}

		/*
		 * Test scenario when no such entity exists in the database
		 */
		@Test
		void shouldReturnEmptyWhenRepositoryDoesNotContainMatch() {
			given(userInfoRepository.findByResourceId(any())).willReturn(Optional.empty());

			// when/then
			Optional<UserInfo> actual = userInfoService.findUserInfoByResourceId(randomSeries.nextResourceId());

			assertThat(actual).isNotNull().isNotPresent();
		}
	}

	@Nested
	class CreateUserInfoTests {

		/*
		 * happy path should create a new entity
		 */
		@Test
		void shouldCreateOneWhenUserInfoIsWellFormed() {
			// given
			final String sampleText = "sample text";
			final UserInfoEntityBean expectedEJB = new UserInfoEntityBean(1L, randomSeries.nextResourceId(),
					sampleText);
			given(userInfoRepository.save(any())).willReturn(expectedEJB);

			// when/then
			UserInfo sampleData = UserInfo.builder().text(sampleText).build();
			UserInfo actual = userInfoService.createUserInfo(sampleData);

			assertThat(actual).isNotNull();
			assertThat(actual.getResourceId()).isNotNull();
			assertThat(actual.getText()).isEqualTo(sampleText);
		}

		@Test
		void shouldThrowNullPointerExceptionWhenUserInfoIsNull() {
			Exception exception = assertThrows(NullPointerException.class, () -> userInfoService.createUserInfo(null));
		}
	}

	@Nested
	class UpdateUserInfoTests {
		/*
		 * Happy path - updates an existing entity
		 */
		@Test
		void shouldUpdateWhenEntityIsFound() {
			// given
			String resourceId = randomSeries.nextResourceId();
			UserInfo changedVersion = UserInfo.builder().resourceId(resourceId).text("new text").build();
			UserInfoEntityBean originalEJB = new UserInfoEntityBean(1L, resourceId, "old text");
			UserInfoEntityBean updatedEJB = userInfoResourceToBeanConverter.convert(changedVersion);
			given(userInfoRepository.findByResourceId(any())).willReturn(Optional.of(originalEJB));
			given(userInfoRepository.save(any())).willReturn(updatedEJB);

			// when/then
			Optional<UserInfo> optional = userInfoService.updateUserInfo(changedVersion);
			then(optional).isNotNull();
			then(optional.isPresent()).isTrue();
			if (optional.isPresent()) {
				then(optional.get().getResourceId()).isEqualTo(resourceId);
				then(optional.get().getText()).isEqualTo("new text");
			}
		}

		/*
		 * When no database record is found, the update should return an empty result
		 */
		@Test
		void shouldReturnEmptyOptionWhenEntityIsNotFound() {
			// given no such entity exists in the database...
			given(userInfoRepository.findByResourceId(any())).willReturn(Optional.empty());

			// then/when
			UserInfo changedVersion = UserInfo.builder().resourceId(randomSeries.nextResourceId()).text("new text")
					.build();
			Optional<UserInfo> result = userInfoService.updateUserInfo(changedVersion);
			then(result.isEmpty()).isTrue();
		}

		@Test
		void shouldThrowExceptionWhenArgumentIsNull() {
			assertThrows(NullPointerException.class, () -> userInfoService.updateUserInfo(null));
		}
	}

	@Nested
	class DeleteUserInfoTests {
		/*
		 * Happy path - deletes an entity
		 */
		@Test
		void shouldDeleteWhenEntityExists() {
			// given one matching one is found
			given(userInfoRepository.deleteByResourceId(any())).willReturn(1L);

			userInfoService.deleteUserInfoByResourceId(randomSeries.nextResourceId());

			// Verify the deleteByResourceId method was invoked
			verify(userInfoRepository, times(1)).deleteByResourceId(any());
		}

		/*
		 * When deleting a non-existing EJB, silently return
		 */
		@Test
		void shouldSilentlyReturnWhenEntityDoesNotExist() {
			// given no matching record is found
			given(userInfoRepository.deleteByResourceId(any())).willReturn(0L);

			userInfoService.deleteUserInfoByResourceId(randomSeries.nextResourceId());

			// Verify the deleteByResourceId method was invoked
			verify(userInfoRepository, times(1)).deleteByResourceId(any());
		}

		@Test
		void shouldThrowExceptionWhenArgumentIsNull() {
			assertThrows(NullPointerException.class, () -> userInfoService.deleteUserInfoByResourceId(null));
		}
	}
}