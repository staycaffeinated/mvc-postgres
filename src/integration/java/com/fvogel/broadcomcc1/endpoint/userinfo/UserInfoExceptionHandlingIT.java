/*
 * Copyright 2022 [CopyrightOwner]
 */
package com.fvogel.broadcomcc1.endpoint.userinfo;

import com.fvogel.broadcomcc1.common.AbstractIntegrationTest;
import com.fvogel.broadcomcc1.math.SecureRandomSeries;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Verify exception handling
 */
@ExtendWith(SpringExtension.class)
class UserInfoExceptionHandlingIT extends AbstractIntegrationTest {

	@MockBean
	private UserInfoService userInfoService;

	final SecureRandomSeries randomSeries = new SecureRandomSeries();

	@Nested
	class ExceptionTests {

		@Test
		void shouldNotReturnStackTrace() throws Exception {
			// given
			given(userInfoService.findUserInfoByResourceId(any(String.class))).willThrow(new RuntimeException("Boom!"));
			given(userInfoService.updateUserInfo(any(UserInfo.class))).willThrow(new RuntimeException("Bad data"));

			UserInfo payload = UserInfo.builder().resourceId(randomSeries.nextResourceId()).text("update me").build();

			// when/then
			mockMvc.perform(post("/api/v1/user-service").contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(payload)))
					.andExpect(jsonPath("$.stackTrace").doesNotExist()).andExpect(jsonPath("$.trace").doesNotExist())
					.andDo((print())).andReturn();
		}
	}
}
