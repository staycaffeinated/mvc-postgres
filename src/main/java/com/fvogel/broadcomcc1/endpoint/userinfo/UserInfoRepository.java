/*
 * Copyright 2022 [CopyrightOwner]
 */
package com.fvogel.broadcomcc1.endpoint.userinfo;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoRepository extends JpaRepository<UserInfoEntityBean, Long> {

	Optional<UserInfoEntityBean> findByResourceId(String id);

	/* returns the number of entities deleted */
	Long deleteByResourceId(String id);

	Page<UserInfoEntityBean> findByText(String text, Pageable pageable);
}
