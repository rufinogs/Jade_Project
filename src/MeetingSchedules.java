package jadeproject;

import java.util.*;

public class MeetingSchedules {
	private boolean[] differentSchedules;

	MeetingSchedules() {

		/*Boolean array of all the different possible schedules in a week (24 hours * 7 days)*/
		differentSchedules = new boolean[168];
		Random randomValue = new Random();

		/*Here we set a random boolean value in each position of the array, true means available and false unavailable*/
		int i=0;
		while (i<differentSchedules.length) {
			differentSchedules[i] = randomValue.nextBoolean();
			i++;
		}
	}

	/*Here we override the toString function in order to add more functionality in the output about the availability*/
	@Override
	public String toString() {
		String ret = "Hours\tMonday\tTuesday\tWednesday\tThursday\tFriday\tSaturday\tSunday\n";

		int hour=0;
		while(hour<24) {

			ret += hour+"H00\t";
			int day=0;

			while(day<7) {
				if (isAvailable(day, hour)==true) {
				    ret += "FREE\t";
				}
				else {
				    ret += "NOFREE\t";
				}
				day++;
			}

			ret += "\n";
			hour++;
		}

		return ret;
	}

	/*This function look for the availability*/
	public ArrayList<int[]> findSlots() {
		ArrayList<int[]> freeSchedules = new ArrayList<int[]>();

		int day=0;
		while (day < 7) {
			int hour=0;
			while (hour < 24) {

				/*If there is any free schedule it is added to the ArrayList*/
				int[] schedule = {day, hour};
				if (isAvailable(day, hour)==true){
					freeSchedules.add(schedule);
				}
				hour++;
			}
			day++;
		}

		return freeSchedules;
	}

	/*This function set a meeting*/
	public void scheduleMeeting(int day, int hour) {
	    differentSchedules[day*24+hour] = true;
	}

	/*This function cancel a meeting*/
  public void cancelMeeting(int day, int hour) {
		differentSchedules[day*24+hour] = false;
	}

	/*This function check the availability of a schedule given*/
	public boolean isAvailable(int day, int hour) {
		return differentSchedules[day*24+hour];
	}

	/*This function returns the respective shorted number of the week days*/
	public static String getWeekDayName(int day) {
		String ret = "";

		if (day==0){ret = "Monday";}
		else if(day==1){ret = "Tuesday";}
		else if(day==2){ret = "Wednesday";}
		else if(day==3){ret = "Thursday";}
		else if(day==4){ret = "Friday";}
		else if(day==5){ret = "Saturday";}
		else if(day==6){ret = "Sunday";}
		else{ret = "Anyday";}

		return ret;
	}
}
