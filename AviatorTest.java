import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class AviatorTest extends JFrame implements ActionListener {
    private int points=100;
    private int wager=0;
    private double multiplier=1.0;
    private boolean isFlying=false;
    private boolean crashed=false;
    private JButton startButton,stopButton,placeBetButton;
    private JTextField betAmountField;
    private JLabel pointsLabel,multiplierLabel,statusLabel,topStatusLabel;
    private Timer flightTimer;
    private Random random;
    private double crashMultiplier;
    private PlanePanel planePanel;
    private List<String> history;

    public AviatorTest() {
        setTitle("Aviator Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top status label for crash/cash-out messages
        topStatusLabel = new JLabel("Welcome to Aviator Game!", SwingConstants.CENTER);
        topStatusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topStatusLabel.setForeground(Color.RED);
        add(topStatusLabel, BorderLayout.NORTH);

        // Panel for betting controls at the bottom
        JPanel bettingPanel = new JPanel(new FlowLayout());
        pointsLabel = new JLabel("Points: " + points);
        multiplierLabel = new JLabel("Multiplier: x" + multiplier);
        statusLabel = new JLabel("Place your bet and start the flight!");
        history = new ArrayList<>();

        betAmountField = new JTextField(5);
        placeBetButton = new JButton("Place Bet of Points");
        startButton = new JButton("Start Flight");
        stopButton = new JButton("Stop");

        placeBetButton.addActionListener(this);
        startButton.addActionListener(this);
        stopButton.addActionListener(this);

        bettingPanel.add(pointsLabel);
        bettingPanel.add(new JLabel("Bet Amount:"));
        bettingPanel.add(betAmountField);
        bettingPanel.add(placeBetButton);
        bettingPanel.add(startButton);
        bettingPanel.add(stopButton);
        bettingPanel.add(multiplierLabel);
        bettingPanel.add(statusLabel);

        add(bettingPanel,BorderLayout.SOUTH);
        planePanel=new PlanePanel();
        add(planePanel,BorderLayout.CENTER);
        flightTimer = new Timer(200, this);
        random = new Random();
        updateButtons();
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        if(e.getSource()==placeBetButton) 
        {
            placeBet();
        } else if(e.getSource()==startButton)
        {
            startFlight();
        } else if(e.getSource()==stopButton) 
        {
            cashOut();
        } else if(e.getSource()==flightTimer) 
        {
            updateFlight();
        }
    }

    private void placeBet() 
    {
        try 
        {
            wager =Integer.parseInt(betAmountField.getText());
            if (wager <=0 || wager >points) 
            {
                statusLabel.setText("Invalid bet. Enter an amount within your points.");
            } else {
                statusLabel.setText("Bet placed: " + wager + " points. Start the flight!");
                updateButtons();
            }
        } catch (NumberFormatException ex) 
        {
            statusLabel.setText("Invalid input. Enter a numeric value.");
        }
    }

    private void startFlight() 
    {
        if(wager >0) 
        {
            isFlying =true;
            crashed =false;
            multiplier =1.00;
            crashMultiplier =1.00 +random.nextDouble()*30;
            topStatusLabel.setText("Flight started! Try to stop out before it crashes!");
            flightTimer.start();
            planePanel.startFlying();
            updateButtons();
        } else 
        {
            statusLabel.setText("Place a bet before starting the flight.");
        }
    }

    private void updateFlight() 
    {
        if (isFlying) 
        {
            multiplier +=0.1;
            multiplierLabel.setText("Multiplier: x" +String.format("%.2f",multiplier));
            planePanel.movePlane();

            if (multiplier >=crashMultiplier) 
            {
                endFlight(false);
            }
        }
    }

    private void cashOut() 
    {
        if (isFlying) 
        {
            int winnings=(int)(wager *multiplier);
            points+=winnings;
            history.add("Bet:" + wager + ",Multiplier: x" +String.format("%.2f",multiplier)+", Winnings: " +winnings);
            endFlight(true);
        }
    }

    private void endFlight(boolean won) 
    {
        isFlying =false;
        flightTimer.stop();
        planePanel.stopFlying();

        if (won) 
        {
            topStatusLabel.setText("You Won " +(int)(wager *multiplier)+ " points.");
        } else 
        {
            points -=wager;
            crashed =true;
            topStatusLabel.setText("Crash! You lost your " + wager + " points.");
            history.add("Bet: " + wager + ", Multiplier: x" +String.format("%.2f",multiplier) + ", Lost: " + wager);
        }

        multiplier =1.0;
        wager =0;
        pointsLabel.setText("Points: " + points);
        multiplierLabel.setText("Multiplier: x" + multiplier);
        updateButtons();
        planePanel.repaint();
    }

    private void updateButtons()
    {
        placeBetButton.setEnabled(!isFlying && wager==0);
        startButton.setEnabled(wager >0 && !isFlying);
        stopButton.setEnabled(isFlying);
    }

    private class PlanePanel extends JPanel 
    {
        private Image planeImage;
        private Image resizedPlaneImage;
        private int planeX =0;
        private int planeY =getHeight()-50; 
        private int riseSpeed =-2;
        private Timer planeTimer;
        private int maxHeight =150;

        public PlanePanel() 
        {
            setBackground(Color.BLACK);

            try 
            {
                planeImage =new ImageIcon("plane.jpg").getImage();
                resizedPlaneImage =planeImage.getScaledInstance(100,100,Image.SCALE_SMOOTH);
            } catch (Exception e) 
            {
                System.out.println("Plane image not found.");
            }

            planeTimer =new Timer(10,new ActionListener() 
            {
                public void actionPerformed(ActionEvent e) 
                {
                    movePlane();
                    repaint();
                }
            });
        }

        public void startFlying() 
        {
            planeX =0;
            planeY =getHeight()-50;
            planeTimer.start();
        }

        public void movePlane() 
        {
            planeX +=5;
            if (planeX >getWidth())planeX =-resizedPlaneImage.getWidth(null);

            if (planeY > maxHeight) 
            {
                planeY += riseSpeed;
            }
        }

        public void stopFlying() 
        {
            planeTimer.stop();
        }

        @Override
        protected void paintComponent(Graphics g) 
        {
            super.paintComponent(g);

            if (resizedPlaneImage != null) 
            {
                g.drawImage(resizedPlaneImage,planeX,planeY -resizedPlaneImage.getHeight(null) /2,this);
            }

            if (!isFlying && crashed) 
            {
                g.setFont(new Font("Arial",Font.BOLD,56));
                g.setColor(Color.white);
                String crashMessage ="Crashed!";
                int stringWidth =g.getFontMetrics().stringWidth(crashMessage);
                int x =(getWidth()-stringWidth) /2;
                int y =getHeight() /2;
                g.drawString(crashMessage,x,y);
            }
        }
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(AviatorTest::new);
    }
}
