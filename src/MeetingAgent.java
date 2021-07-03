package jadeproject;

import java.util.*;
import jade.core.Agent;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import jade.lang.acl.UnreadableException;
import jade.lang.acl.ACLMessage;

import jade.core.AID;
import jade.core.behaviours.*;

import jade.lang.acl.MessageTemplate;

public class MeetingAgent extends Agent {

  /*The random value*/
  private Random randomValue = new Random();
  /*A defined timeout*/
	private final long TIMEOUT = 1000;
  /*The gui*/
  private GuiAgent gui;
  /*The agent*/
	private final String agent = "meeting-agent";
  /*Flag for set a meeting*/
	private boolean flagMeeting = false;
  /*The agent's calendar*/
	private MeetingSchedules agentCalendar = new MeetingSchedules();
  /*The arraylist with the availability*/
	ArrayList<int[]> availableMeetings= agentCalendar.findSlots();


	@Override
	protected void setup() {

    /*We start displaying the gui in the screen with a message of beggining*/
		gui = new GuiAgent(this);
		gui.display();
		System.out.println(getAID().getLocalName() + "--> I'm ready!");

    /*Here we set a name and description of the agent*/
		DFAgentDescription dfAgentDescription = new DFAgentDescription();
		dfAgentDescription.setName(getAID());

    /*Here we set a description for the agent's service*/
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType(agent);
		serviceDescription.setName(getAID().getLocalName());
    /*Here we add the service description*/
		dfAgentDescription.addServices(serviceDescription);

    /*Here we set the agent with its description*/
		try {
			DFService.register(this, dfAgentDescription);
		} catch (FIPAException ex) {
			ex.printStackTrace();
		}

    /*Here we add the behaviours of the agent:*/
    /*Cancelation:*/
    addBehaviour(new ReceiveCancelation());
    /*Invitation:*/
  	addBehaviour(new ReceiveInvitation());
    /*Confirmation:*/
		addBehaviour(new ReceiveConfirmation());
	}

  /*For program a meeting*/
	public void programMeeting() {
		System.out.println(getAID().getLocalName() + "--> is looking forward to set a meeting!");
		flagMeeting = true;
		addBehaviour(new MainBehaviour());
	}

  /*For finish the agent tasks*/
	@Override
	protected void takeDown() {
		gui.dispose();
		try {
			DFService.deregister(this);
		} catch (FIPAException ex) {
			ex.printStackTrace();
		}
		System.out.println(getAID().getLocalName() + "--> See you!!");
	}

	private class MainBehaviour extends Behaviour {
    /*Number of participants*/
		private int participantsNumber=0;
    /*Where the program jumps in every moment*/
		private int program=0;
    /*Beggining and ending of the time*/
    private long beggining, ending;
    /*Set of the available meeting schedules*/
		private HashSet<int[]> meetingSchedules = new HashSet<int[]>();
    /*The agent's template */
		private MessageTemplate template;

    /*The arrays of participants, agents and free schedules*/
    private AID[] arrayParticipants;
		private AID[] arrayAgents;
		private int[] arrayFreeSchedules;

    /*Auxiliary counters*/
    private int auxCounter1=0;
    private int auxCounter2=0;

		@Override
		public void action() {
			if (flagMeeting == true) {
				switch (program) {
					case 0:
            /*Beggining message*/
						System.out.println(myAgent.getLocalName() + "--> is searching participants for the meeting...");

            /*Here we set the agent type and the service to the template*/
						DFAgentDescription dfTemplate = new DFAgentDescription();
						ServiceDescription serviceDescription = new ServiceDescription();
						serviceDescription.setType(agent);
						dfTemplate.addServices(serviceDescription);

						try {
              /*We search people to be invited to the meeting*/
							DFAgentDescription[] result = DFService.search(myAgent, dfTemplate);

							System.out.println(getAID().getLocalName() + "--> Some candidates were found:");
							arrayAgents = new AID[result.length - 1];

							int var=0;
              int i=0;
              /*Here we look for different participants and we show them in the screen*/
							while (i<result.length){
								if (!result[i].getName().equals(getAID().getName())) {
									arrayAgents[var] = result[i].getName();
									System.out.println(myAgent.getLocalName() + "--> found '" + arrayAgents[var++].getLocalName() + "'");
								}
                i++;
							}
						} catch (FIPAException ex) {
							ex.printStackTrace();
						}

						System.out.println(myAgent.getLocalName() + "--> is thinking who will attend to the meeting...");

						int maximum = arrayAgents.length;
						System.out.println("SYSTEM --> The maximum number of people to invite is--> "+ maximum);

            /*We decide randomly who will attend to the meeting*/
						if (maximum>0) {
              participantsNumber = randomValue.nextInt(maximum)+1;
            }
						System.out.println(getAID().getLocalName() + "--> decided that " + participantsNumber + " participant/s will attend to the meeting!");

						if (participantsNumber != 0) {
							arrayParticipants = new AID[participantsNumber];
							System.out.println(getAID().getLocalName() + "--> Participant/s invited: ");

              int i=0;
							while (i<participantsNumber) {
								if (!arrayAgents[i].getName().equals(getAID().getName())) {
					          arrayParticipants[i] = arrayAgents[i];
	                  System.out.println(getAID().getLocalName() + "--> Participant number " + (i+1) + ": '" + arrayParticipants[i].getLocalName() +"'");
								}
                i++;
							}

							System.out.println(getAID().getLocalName() + "--> Is deciding the meeting schedule...");

              /*We take it randomly from the list*/
							arrayFreeSchedules = availableMeetings.get(randomValue.nextInt(availableMeetings.size()-1));

							if (arrayFreeSchedules==null) {
								/*In case that there is no free time*/
								System.out.println(getAID().getLocalName() + "--> there is no free time for the meeting");
								program = 10;
							}
              else {
                /*Here we decide a day and a hour for schedule the meeting*/
					        int day = arrayFreeSchedules[0];
                  int hour = arrayFreeSchedules[1];
                  System.out.println(getAID().getLocalName() + "--> Proposed schedule: " + MeetingSchedules.getWeekDayName(day) + " at " + hour + ":00");
                  /*Here we add that schedule to the array we have for it and we move forward*/
			            meetingSchedules.add(arrayFreeSchedules);
				          program = 1;
							}
						}
            else {
              /*In case that there is no participants we jump to the end of the program*/
							program = 10;
						}
						break;

					case 1:
						beggining = System.currentTimeMillis();

						System.out.println(getAID().getLocalName() + "--> Is sending the invitations to the participants! Waiting for the response...");
            /*We create the message that is going to be sent*/
						ACLMessage message = new ACLMessage(ACLMessage.CFP);

            /*We add here the receptors of the message, the participants*/
						int i=0;
						while(i<participantsNumber){
							message.addReceiver(arrayParticipants[i]);
							i++;
						}

            /*We determine in the system the day and the hour*/
						message.setContent(Integer.toString(arrayFreeSchedules[0]) + "," + Integer.toString(arrayFreeSchedules[1]));
						message.setConversationId("schedule-meeting");
						message.setReplyWith("cfp" + System.currentTimeMillis());

            /*Here we put available the schedule that we took as choice, for other meetings*/
						agentCalendar.scheduleMeeting(arrayFreeSchedules[0], arrayFreeSchedules[1]);
						availableMeetings = agentCalendar.findSlots();

            /*Here we send the message to the participants*/
						myAgent.send(message);
						//System.out.println(getAID().getLocalName() + "--> All the invitations were sent! Waiting for the response...");

            /*Here we match the template with the previous id*/
						template = MessageTemplate.and(MessageTemplate.MatchConversationId("schedule-meeting"),
						MessageTemplate.MatchInReplyTo(message.getReplyWith()));

						program = 2;
						auxCounter1=0;
						break;

					case 2:
						ACLMessage reply = myAgent.receive(template);

						ending = System.currentTimeMillis();
						double difference = ending-beggining;

            /*If the agent pass a certain timeout we cancel and move to the end of the program*/
						if (difference>TIMEOUT) {
							cancelationMessage(Integer.toString(arrayFreeSchedules[0]) + "," + Integer.toString(arrayFreeSchedules[1]), null);
							program = 10;
						}
            else {
              /*Once we get a reply*/
							if (reply != null) {
                /*In case the reply is rejected*/
								if (reply.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
									System.out.println(getAID().getLocalName() + "--> '" + reply.getSender().getLocalName() + "' rejected the meeting!");

                  /*We get the participant who rejected the meeting*/
									AID rejectingAgent = reply.getSender();

									int day_cancel = arrayFreeSchedules[0];
                  int hour_cancel = arrayFreeSchedules[1];

                  /*Here we cancel the initial meeting and we look for another schedule*/
									agentCalendar.cancelMeeting(day_cancel, hour_cancel);
									availableMeetings = agentCalendar.findSlots();

                  /*Here we remove the proposed meeting from the arraylist*/
									for (int[] proposedMeetingTime:meetingSchedules) {
										availableMeetings.remove(proposedMeetingTime);
									}

                  /*We make it random again*/
									if (availableMeetings.size()>1){
                    arrayFreeSchedules = availableMeetings.get(randomValue.nextInt(availableMeetings.size()-1));
                  }
                  /*For when only one schedule is available*/
                  else if (availableMeetings.size()==1){
                    arrayFreeSchedules = availableMeetings.get(0);
                  }
                  /*When all schedules are unavailable*/
									else {
                    arrayFreeSchedules = null;
                  }

                  /*When we do not have more schedules we go to the end of the program*/
									if (arrayFreeSchedules==null) {
										System.out.println(getAID().getLocalName() + "--> there is no free time for the meeting");
										program = 10;
									}
                  /*We change the meeting schedule*/
                  else {
										int day = arrayFreeSchedules[0];
                    int hour = arrayFreeSchedules[1];

										System.out.println(getAID().getLocalName() + "--> Is modifing the meeting schedule... it finally will be on: " + MeetingSchedules.getWeekDayName(day) + " at " + hour + ":00");
                    /*And we move back to the sending invitations process*/
										program = 1;
									}

									System.out.println(getAID().getLocalName() + "--> Is sending again the invitations with the new meeting schedule...");
                  /*Here we send a cancelation message because of the rejection*/
									cancelationMessage(Integer.toString(day_cancel) + "," + Integer.toString(hour_cancel), rejectingAgent);

								}
                /*In case everything goes good the meeting schedule is accepted*/
                else {
									System.out.println(getAID().getLocalName() + "--> '" + reply.getSender().getLocalName() + "' accepted the meeting succesfully!");
									auxCounter1++;
                  /*Once all responses were recived we move forward*/
									if (auxCounter1==arrayParticipants.length) {
										program = 3;
									}
								}
							}
              else {
								block(TIMEOUT);
							}
						}
						break;

					case 3:
            /*Here we send a confirmation message of the meeting schedule*/
						System.out.println(getAID().getLocalName() + "--> Is sending the confirmation message of the meeting...");

            /*We create the message and add the receptors*/
						beggining = System.currentTimeMillis();
						ACLMessage confirmationMessage = new ACLMessage(ACLMessage.CONFIRM);
            int k=0;
						while (k<participantsNumber) {
							confirmationMessage.addReceiver(arrayParticipants[k]);
              k++;
						}
            /*The message content is the exact meeting schedule*/
						confirmationMessage.setContent(Integer.toString(arrayFreeSchedules[0]) + "," + Integer.toString(arrayFreeSchedules[1]));
						confirmationMessage.setConversationId("schedule-meeting");
						confirmationMessage.setReplyWith("confirm" + System.currentTimeMillis());

						myAgent.send(confirmationMessage);

						System.out.println(getAID().getLocalName() + "--> Already sent all the confirmation messages to the participants of the meeting");

            /*Here we do the same as before matching the template*/
						template = MessageTemplate.and(MessageTemplate.MatchConversationId("schedule-meeting"),
						MessageTemplate.MatchInReplyTo(confirmationMessage.getReplyWith()));
						auxCounter2=0;
						program = 4;

					case 4:

						ACLMessage AttendanceConfirmed = myAgent.receive(template);

						ending = System.currentTimeMillis();
						difference = ending-beggining;

            /*Here we check if the participants answered to the confirmed meeting, otherwise we jump to the end of the program*/
						if (difference > TIMEOUT) {
							cancelationMessage(Integer.toString(arrayFreeSchedules[0]) + "," + Integer.toString(arrayFreeSchedules[1]), null);
							program = 10;
						}
            else {
							if (AttendanceConfirmed != null) {
								if (AttendanceConfirmed.getPerformative() == ACLMessage.AGREE) {
									AID agent = AttendanceConfirmed.getSender();
                  /*We inform about the confirmation attendance*/
									System.out.println(getAID().getLocalName() + "--> Received the ¡¡¡FINAL!!! confirmation attendance message from '" + agent.getLocalName() + "'");

									auxCounter2++;
                  /*Once we have all the confirmation attendance messages we move forward*/
									if (auxCounter2==arrayParticipants.length) {
										program = 5;
									}
								}
							}
              else {
								block(TIMEOUT);
							}
						}
						break;
					case 5:
            /*Here we set finally a confirmed meeting schedule*/
						int day = arrayFreeSchedules[0], hour = arrayFreeSchedules[1];

						System.out.println(getAID().getLocalName() + "--> Finally the meeting schedule will be on " + MeetingSchedules.getWeekDayName(day) + " at " + hour + ":00");
            /*In any other error case we jump to the end of the program*/
          default:
						System.out.println(getAID().getLocalName() + "--> Finally the meeting is not existing anymore");
						program = 10;
						break;

				}
			}
		}

    /*This function have the task of send a cancelation message to the participans who accepted the invitation*/
		private void cancelationMessage(String content, AID agentReject) {

			System.out.println(getAID().getLocalName() + "--> Is now sending cancelation messages to all participants who accepted the invitation...");

      /*Here we create the message and set the receptors of it*/
			ACLMessage cancelationMessage = new ACLMessage(ACLMessage.CANCEL);
			int i=0;
			while (i<participantsNumber){
				if (agentReject != null) {
					if (!agentReject.equals(arrayParticipants[i])){
             cancelationMessage.addReceiver(arrayParticipants[i]);
	        }
        }
				i++;
			}

      /*Here we set the content and send the message*/
			cancelationMessage.setContent(content);
			cancelationMessage.setConversationId("schedule-meeting");
			cancelationMessage.setReplyWith("cancel" + System.currentTimeMillis());
			myAgent.send(cancelationMessage);

      /*Here we show it in the output*/
			System.out.println(getAID().getLocalName() + "--> Already sent all the cancelation messages");
		}


    /*This function check if the the jumps of the program went well or not, changing the flag*/
		@Override
		public boolean done() {
			if (program==5 || program==10) {
				flagMeeting=false;
				return true;
			}
      else {
				return false;
			}
		}
	}

  /*Function for determine the behaviour of receiving a invitation to a meeting*/
	private class ReceiveInvitation extends CyclicBehaviour  {

		@Override
		public void action() {
			MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage message = myAgent.receive(template);

      /*Here we create the message with the template*/
			if (message != null) {
				AID invitingAgent = message.getSender();

				System.out.println(getAID().getLocalName() + "--> Just received an invitation to a meeting, invited by '" + invitingAgent.getLocalName()+"'");

        /*Here we define the content of the message with the proper schedule*/
				String messageContent = message.getContent();
				String[] day_hour = messageContent.split(",");

				int day;
        int hour;
				day = Integer.parseInt(day_hour[0]);
				hour = Integer.parseInt(day_hour[1]);

				System.out.println(getAID().getLocalName() + "--> Received a meeting invitation in the following schedule: " + MeetingSchedules.getWeekDayName(day) + " at " +  hour + ":00. Checking availability...");

				if (agentCalendar.isAvailable(day, hour)) {
					System.out.println(getAID().getLocalName() + "--> Is available for attend the meeting! Preparing aceptance message...");

          /*We set here the meeting schedule*/
					agentCalendar.scheduleMeeting(day, hour);

          /*Here we create a aceptance message of the meeting with the schedule in the content*/
					ACLMessage acceptance = message.createReply();
					acceptance.setContent(messageContent);
					acceptance.setPerformative(ACLMessage.INFORM);

          /*And we send the acceptance message*/
					myAgent.send(acceptance);
					System.out.println(getAID().getLocalName() + "--> Aceptance message succesfully sent! Waiting for the host confirmation...");

				}
        else {
          /*In the opposite case...*/
					System.out.println(getAID().getLocalName() + "--> Is unavailable for attend the meeting! Preparing rejection message...");

          /*We follow the same process as before*/
					ACLMessage rejection = message.createReply();
					rejection.setContent(messageContent);
					rejection.setPerformative(ACLMessage.REJECT_PROPOSAL);
					myAgent.send(rejection);

					System.out.println(getAID().getLocalName() + "--> Rejection message succesfully sent! Waiting for any other meeting invitations...");

				}
			}
      else {
				block();
			}
		}
	}

  /*Function for determine the behaviour of receiving a cancelation notification to a meeting*/
	private class ReceiveCancelation extends CyclicBehaviour {
		@Override
		public void action() {
      /*Here we create the message with the template*/
			MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
			ACLMessage message = myAgent.receive(template);

			if (message != null) {
				AID cancelingAgent = message.getSender();

        /*Here we create the message content*/
				String messageContent = message.getContent();
				String[] day_hour = messageContent.split(",");

				int day;
        int hour;
				day = Integer.parseInt(day_hour[0]);
				hour = Integer.parseInt(day_hour[1]);
				int[] meetingTime = {day, hour};

				System.out.println(getAID().getLocalName() + "--> Just received a cancelation message from '" + cancelingAgent.getLocalName()  + "' informing about a meeting in the following schedule: " + MeetingSchedules.getWeekDayName(day) + " at " + hour + ":00");

				agentCalendar.cancelMeeting(day, hour);

				System.out.println(getAID().getLocalName() + "--> The meeting which was going to take place on: " + MeetingSchedules.getWeekDayName(day) + " at " + hour + ":00 was cancelled");
			}
      else {
				block(TIMEOUT);
			}
		}
	}

  /*Function for determine the behaviour of receiving a cancelation notification to a meeting*/
	private class ReceiveConfirmation extends CyclicBehaviour {
		@Override
		public void action() {
      /*We create the message with the template*/
			MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
			ACLMessage message = myAgent.receive(template);

			if (message != null) {
				ACLMessage AttendanceConfirmed = message.createReply();

        /*Here we get the message content*/
				String messageContent = message.getContent();
				String[] day_hour = messageContent.split(",");

				int day;
        int hour;
				day = Integer.parseInt(day_hour[0]);
				hour = Integer.parseInt(day_hour[1]);

				System.out.println(getAID().getLocalName() + "--> Just received the confirmation message of the meeting on the following schedule: " + MeetingSchedules.getWeekDayName(day) + " at " +  hour + ":00. Sending the final attendance message...");

        /*We create the message and then send it*/
				AttendanceConfirmed.setContent(messageContent);
				AttendanceConfirmed.setPerformative(ACLMessage.AGREE);
				myAgent.send(AttendanceConfirmed);

				System.out.println(getAID().getLocalName() + "--> Final attendance message succesfully sent!");
			}
      else {
				block(TIMEOUT);
			}
		}
	}
}
