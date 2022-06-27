/*
 * Copyright 2022 [CopyrightOwner]
 */

package com.fvogel.broadcomcc1.endpoint.userinfo;

import com.fvogel.broadcomcc1.math.SecureRandomSeries;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserInfoResourceToBeanConverterTests {

	UserInfoResourceToBeanConverter converter = new UserInfoResourceToBeanConverter();

	final SecureRandomSeries randomSeries = new SecureRandomSeries();

	@Test
	void shouldReturnNullWhenResourceIsNull() {
		assertThrows(NullPointerException.class, () -> {
			converter.convert((UserInfo) null);
		});
	}

	@Test
	void shouldReturnNullWhenListIsNull() {
		assertThrows(NullPointerException.class, () -> {
			converter.convert((List<UserInfo>) null);
		});
	}

	@Test
	void shouldPopulateAllFields() {
		UserInfo resource = UserInfo.builder().resourceId(randomSeries.nextResourceId()).text("hello world").build();

		UserInfoEntityBean bean = converter.convert(resource);
		assertThat(bean.getResourceId()).isEqualTo(resource.getResourceId());
		assertThat(bean.getText()).isEqualTo(resource.getText());
	}

	@Test
	void shouldCopyList() {
		UserInfo resource = UserInfo.builder().resourceId(randomSeries.nextResourceId()).text("hello world").build();
		var pojoList = Lists.list(resource);

		List<UserInfoEntityBean> ejbList = converter.convert(pojoList);
		assertThat(ejbList.size()).isOne();
		assertThat(ejbList.get(0).getResourceId()).isEqualTo(resource.getResourceId());
		assertThat(ejbList.get(0).getText()).isEqualTo(resource.getText());
	}
}
