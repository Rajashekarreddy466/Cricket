package com.cricket.service;

import java.util.Comparator;
import java.util.PriorityQueue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cricket.model.PlayerProfile;

@Service
public class CricketService {
	
	
	@Value("${api_token}") 
	private String api_key;
	
	public String gettopplayers(int no_of_batsmens, int no_of_bowlers, int no_of_allrounders) {
		
		String topPlayersString = "";
		//From this method we call players based on the number provided by the user.
		//Main point is if we want to decide based on multiple parameters then go for point based system(decide point based system on your approach).
		//1. for getting number of batsmens provided by user along with one keeper(based on strike rate).
		//2. for getting number of bowlers provided by user(criteria not yet decided).
		//3. for getting number of allrounders provided by user(criteria not yet decided).
		
		//season_id = 13 and country_id = 52126 pass it dynamically.
		String topBatters = "";
		String topWicketKeeper = "";
		int no_of_batters = no_of_batsmens-1;
		//calling first method.
		if(no_of_batters > 0) {
			//if batsmens are greater than 0 then call one keeper and remaining batsmens.
			topBatters = getTopPlayersInrespectiveDepartment(no_of_batters, 1, "batting", 13);
			topWicketKeeper = getTopPlayersInrespectiveDepartment(1, 3, "batting", 13);
		}
		else {
			//only one wk required without batsmens.
			topWicketKeeper = getTopPlayersInrespectiveDepartment(1, 3, "batting", 13);
		}
		//Calling second method.
		String topBowlers = getTopPlayersInrespectiveDepartment(no_of_bowlers, 2, "bowling", 13);
		//Calling third method.
		String topAllrounders = getTopPlayersInrespectiveDepartment(no_of_allrounders, 4, "allrounder", 13);
		//returning the main string containing top 11 players.
		topPlayersString = topBatters + topWicketKeeper + topAllrounders +topBowlers;
		return topPlayersString;
				
	}
	
	public String getTopPlayersInrespectiveDepartment(int no_of_required_players, int position_id, String department, int season_id) {
		//Get the api data and sort according to the given criteria and send back to user.
		String params = "?include=career&api_token="+api_key+"&filter[position_id]="+position_id+"&filter[country_id]=52126";
		String url = "https://cricket.sportmonks.com/api/v2.0/players/" + params;
		RestTemplate restTemaplate = new RestTemplate();
				
		String data = restTemaplate.getForObject(url, String.class);
				
		JSONObject json = new JSONObject(data);
				
		//Getting all the players array including carrers.
		JSONArray array = json.getJSONArray("data");
		//Getting respective ODI careers or a particular format careers(chossen T20 career 13 season_id).
		//Iterate through the json array and then store name, strike rate and average in particular object (lets say json object).
		//Maybe get the data based on filtered season id also so that only few careers will be present in careers array which will
		//be easy to sort and maintain.
				
		PriorityQueue<PlayerProfile> topPlayers = new PriorityQueue<>(new Comparator<PlayerProfile>() {
			@Override
			public int compare(PlayerProfile o1, PlayerProfile o2) {
				if(o1.getPoints() < o2.getPoints()) return 1;
				else if(o1.getPoints() > o2.getPoints()) return -1;
				return 0;
			}
		});
		
				
		//For sorting use a model class and also priority queue for storing batsmens or bowlers or allrounders based on no of players required in each categeory.
		for(int i=0;i<array.length();i++) {
			PlayerProfile playerProfile = new PlayerProfile();
			JSONObject player = array.getJSONObject(i);
			JSONArray playerCareers = player.getJSONArray("career");
			JSONObject format = null;
			JSONObject playerStats = null;
					
			for(int j=0;j<playerCareers.length();j++) {
				format = playerCareers.getJSONObject(j);
				if(format.getInt("season_id") == season_id) {
					if(department.equalsIgnoreCase("bowling") || department.equalsIgnoreCase("batting")) {
						playerStats = format.getJSONObject(department);
					}
							
					//We got required format for getting top players.
					//store the details in the PlayerProfile class object.
					if(department.equalsIgnoreCase("batting")) {
						playerProfile = settingBattingDataIntoProfile(playerStats, player, playerProfile);
					}
					else if(department.equalsIgnoreCase("bowling")) {
						playerProfile = settingBowlingDataIntoProfile(playerStats, player, playerProfile);
					}
					else {
						playerProfile = settingAllrounderDataIntoProfile(format ,player);
					}
					
					//store the details in priority queue based on strike rate.
					topPlayers.add(playerProfile);
							
					break;
				}
			}
					
		}
				
		String mainString = "";
		int i=0;
		while(!topPlayers.isEmpty() && i<no_of_required_players) {
			mainString = mainString + topPlayers.poll().toString();
			i++;
		}
		topPlayers.clear();
		return mainString;
	}
	

	public PlayerProfile settingAllrounderDataIntoProfile(JSONObject format ,JSONObject player) {
		PlayerProfile playerProfile = new PlayerProfile();
		int batting_points = 0;
		int bowling_points = 0;
		int total_points = 0;
		playerProfile.setAllrounder(true);
		//call both methods batting and bowling.
		if(format.get("batting") instanceof JSONObject) {
			JSONObject playerBattingStats = format.getJSONObject("batting");
			//call batting points calculation method.
			playerProfile = settingBattingDataIntoProfile(playerBattingStats, player, playerProfile);
			batting_points = playerProfile.getPoints();
		}
		if(format.get("bowling") instanceof JSONObject) {
			JSONObject playerBowlingStats = format.getJSONObject("bowling");
			//call bowling points calculation method.
			playerProfile = settingBowlingDataIntoProfile(playerBowlingStats, player, playerProfile);
			bowling_points = playerProfile.getPoints();
		}
		
		total_points = batting_points + bowling_points;
		playerProfile.setPoints(total_points);
		
		return playerProfile;
	}
	
	public PlayerProfile settingBattingDataIntoProfile(JSONObject playerStats, JSONObject player, PlayerProfile playerProfile) {
		//PlayerProfile playerProfile = new PlayerProfile();
		playerProfile.setBatting(true);
		playerProfile.setStrikeRate((playerStats.getBigDecimal("strike_rate")).intValue());
		playerProfile.setFullName(player.getString("fullname"));
		playerProfile.setAverage((playerStats.getBigDecimal("average")).intValue());
		//points also needs to be calculated here.
		int total_points = 0;
		int runs_points = playerStats.getInt("runs_scored")*1;
		int four_points = playerStats.getInt("four_x")*1;
		int six_points = playerStats.getInt("six_x")*2;
		int average_points = 0;
		int strike_points = 0;
		int average = playerProfile.getAverage();
		if(average >= 30 && average < 40) average_points = 4;
		else if(average >= 40 && average < 50) average_points = 6;
		else if(average >= 50) average_points = 8;
		int strikerate = playerProfile.getStrikeRate();
		if(strikerate >= 165) strike_points = 6;
		else if(strikerate >=145 && strikerate < 165) strike_points = 4;
		else if(strikerate >=120 && strikerate < 145) strike_points = 2;
		total_points = runs_points + four_points + six_points + average_points + strike_points;
		playerProfile.setPoints(total_points);
			
		return playerProfile;
	}
	
	public PlayerProfile settingBowlingDataIntoProfile(JSONObject playerStats, JSONObject player, PlayerProfile playerProfile) {
		//PlayerProfile playerProfile = new PlayerProfile();
		playerProfile.setBowling(true);
		playerProfile.setWickets(playerStats.getInt("wickets"));
		playerProfile.setFullName(player.getString("fullname"));
		playerProfile.setEconomy((playerStats.getBigDecimal("econ_rate")).intValue());
		//points also needs to be calculated.
		int total_points = 0;
		int wicket_points = playerProfile.getWickets()*25;
		int four_wicket_points = playerStats.getInt("four_wickets")*8;
		int five_wicket_points = playerStats.getInt("five_wickets")*16;
		int economy_points = 0;
		int economy = playerProfile.getEconomy();
		if(economy < 6) economy_points = 6;
		else if(economy >= 6 && economy < 8) economy_points = 4;
		else if(economy >= 8 && economy < 10) economy_points = 2;
		total_points = wicket_points + economy_points + four_wicket_points + five_wicket_points;
		playerProfile.setPoints(total_points);
		
		return playerProfile;
	}
	
}
