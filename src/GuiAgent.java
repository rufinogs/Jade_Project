package jadeproject;

import jade.core.AID;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class GuiAgent extends JFrame{

    /*The agent of the gui*/
    private MeetingAgent guiAgent;

    GuiAgent(MeetingAgent age) {
		super(age.getLocalName());
		guiAgent = age;

    /*We create a panel for each participant*/
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 1));

    /*We set a font for the text inside the button*/
    Font font = new Font ("Times New Roman", 3, 20);

    /*Then we create a button for schedule a meeting*/
		JButton b1 = new JButton("Schedule a meeting");
    b1.setBackground(Color.green);
    b1.setFont(font);

    /*Here it calls the function for schedule a meeting*/
		b1.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent ev) {
				try {
          guiAgent.programMeeting();
				}
        catch (Exception e) {
					JOptionPane.showMessageDialog(GuiAgent.this, e.getMessage());
				}
			}
		});

    /*Here we customize the button*/
		b1.setPreferredSize(new Dimension(300,50));
		panel.add(b1);

		getContentPane().add(panel, BorderLayout.SOUTH);

    /*For close the panel of the button*/
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				guiAgent.doDelete();
			}
		});
    }

    /*Here we determine some properties of the display*/
    public void display(){
        pack();
		    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	      int X = (int)dim.getWidth()/2;
        int Y = (int)dim.getHeight()/2;
        setLocation(X-getWidth()/2, Y-getHeight()/2);
        setVisible(true);
  }
}
