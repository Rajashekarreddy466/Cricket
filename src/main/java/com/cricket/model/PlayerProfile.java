package com.cricket.model;

import java.math.BigDecimal;
import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerProfile {
	
	private String fullName;
	private int strikeRate;
	private int average;
	private int wickets;
	private int economy;
	private int points;
	private boolean bowling = false;
	private boolean batting = false;
	private boolean allrounder = false;

}
