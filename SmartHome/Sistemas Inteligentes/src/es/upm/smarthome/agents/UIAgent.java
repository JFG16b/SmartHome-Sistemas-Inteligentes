package es.upm.smarthome.agents;

import es.upm.smarthome.AgentBase;
import es.upm.smarthome.AgentModel;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UIAgent extends AgentBase {
    private JFrame frame;
    private JTextArea historyArea, dataArea;
    private String[] datosL;
    private Map<String, JLabel> stateLabels;  // acción -> JLabel del estado (ON/OFF)
    private final String[] actions = {"Calefaccion", "AireAcondicionado", "Ventilacion", "Riego", "Persiana"};

    protected void setup() {
        super.setup();
        this.type = AgentModel.UI;
        registerAgentDF();

        SwingUtilities.invokeLater(this::createAndShowGUI);

        addBehaviour(new CyclicBehaviour() {
            private MessageTemplate template = MessageTemplate.MatchOntology("action-ontology");

            public void action() {
                ACLMessage msg = receive(template);
                if (msg != null) {
                    String content = msg.getContent();
                    log("Action received: " + content);
                    updateUI(content);
                } else {
                    block();
                }
            }
        });
    }

    private void createAndShowGUI() {
        frame = new JFrame("Smart Home Control Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // Panel para las acciones (con imágenes y textos ON/OFF)
        JPanel actionsPanel = new JPanel(new GridLayout(1, actions.length, 15, 10));
        actionsPanel.setBorder(BorderFactory.createTitledBorder("Estado de los dispositivos"));
        stateLabels = new HashMap<>();

        for (String act : actions) {
            // Tarjeta individual para cada acción
            JPanel card = new JPanel(new BorderLayout(5, 5));
            card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            card.setPreferredSize(new Dimension(110, 110));

            // Cargar imagen fija (una sola por acción, p.ej. "Calefaccion.png")
            ImageIcon icon = loadImage(act + ".png");
            JLabel iconLabel = new JLabel();
            if (icon != null) {
                Image img = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(img));
            } else {
                iconLabel.setText(act);
                iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            }
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

            // Texto de estado (ON/OFF)
            JLabel stateLabel = new JLabel("OFF", SwingConstants.CENTER);
            stateLabel.setFont(new Font("Arial", Font.BOLD, 14));
            stateLabel.setForeground(Color.RED);
            stateLabels.put(act, stateLabel);

            card.add(iconLabel, BorderLayout.CENTER);
            card.add(stateLabel, BorderLayout.SOUTH);
            actionsPanel.add(card);
        }

        // Área de historial
        historyArea = new JTextArea(8, 40);
        historyArea.setEditable(false);
        JScrollPane scrollHistory = new JScrollPane(historyArea);
        scrollHistory.setBorder(BorderFactory.createTitledBorder("Historial de acciones"));

        dataArea = new JTextArea(4, 40);
        dataArea.setEditable(true);
        JScrollPane scrollData = new JScrollPane(dataArea);
        scrollData.setBorder(BorderFactory.createTitledBorder("Datos recibidos"));

        frame.add(actionsPanel, BorderLayout.NORTH);
        frame.add(scrollHistory, BorderLayout.CENTER);
        frame.add(scrollData, BorderLayout.SOUTH);

        frame.setSize(700, 450);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
    }

    private void updateUI(String actionMessage) {
        SwingUtilities.invokeLater(() -> {
        	String[] valueParts = actionMessage.split("\\|");
            String actionsPart = valueParts[1];
            if (valueParts[1].startsWith("ACCION:")) {
                actionsPart = valueParts[1].substring(7);
            }

            // Primero ponemos todos los estados a OFF (por defecto)
            for (String act : actions) {
                JLabel lbl = stateLabels.get(act);
                lbl.setText("OFF");
                lbl.setForeground(Color.RED);
            }

            // Procesamos cada acción individual (separadas por ;)
            String[] tokens = actionsPart.split(";");
            for (String token : tokens) {
                if (token.isEmpty()) continue;
                // token ej: "Calefaccion_ON" o "Persiana_ABIERTA" (no lleva _ON sino _CERRADA/_ABIERTA)
                // Para persiana usamos "ABIERTA" como equivalente a ON y "CERRADA" a OFF
                String[] parts = token.split("_");
                if (parts.length == 2) {
                    String actionName = parts[0];
                    String state = parts[1];
                    if (stateLabels.containsKey(actionName)) {
                        JLabel lbl = stateLabels.get(actionName);
                        boolean isOn = state.equals("ON") || state.equals("ABIERTA");
                        lbl.setText(isOn ? "ON" : "OFF");
                        lbl.setForeground(isOn ? Color.GREEN : Color.RED);
                    }
                }
            }

            // Añadir al historial con timestamp
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            historyArea.append("[" + timestamp + "] " + actionsPart + "\n");
            historyArea.setCaretPosition(historyArea.getDocument().getLength());
            
            datosL = valueParts[0].split(";");
            dataArea.setText("Temperatura: " + datosL[0] + "°C" + "\n" + "Humedad: " + datosL[1] + "%" + "\n" + "Velocidad del viento: " + datosL[2] + " Km/h" + "\n" + "Lluvia: " + datosL[3]);
        }); 
    }

    private ImageIcon loadImage(String fileName) {
        // Busca la imagen en la carpeta "images" dentro del classpath (src/images)
        java.net.URL url = getClass().getClassLoader().getResource("images/" + fileName);
        if (url == null) {
            System.err.println("No se pudo cargar la imagen: images/" + fileName);
            return null;
        }
        return new ImageIcon(url);
    }
}