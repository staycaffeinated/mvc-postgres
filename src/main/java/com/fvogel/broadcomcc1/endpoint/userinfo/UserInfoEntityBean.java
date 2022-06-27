/*
 * Copyright 2022 [CopyrightOwner]
 */

package com.fvogel.broadcomcc1.endpoint.userinfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "UserInfo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoEntityBean {

	/*
	 * This identifier is never exposed to the outside world because
	 * database-generated Ids are commonly sequential values that a hacker can
	 * easily guess.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 * This is the identifier exposed to the outside world. This is a secure random
	 * value with at least 160 bits of entropy, making it difficult for a hacker to
	 * guess. This is a unique value in the database. This value can be a positive
	 * or negative number.
	 */
	@Column(name = "resource_id", nullable = false)
	private String resourceId;

	@Column(name = "text", nullable = false)
	@NotEmpty(message = "Text cannot be empty")
	private String text;
}