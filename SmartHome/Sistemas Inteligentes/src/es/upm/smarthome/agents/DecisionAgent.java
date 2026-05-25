package es.upm.smarthome.agents;

import es.upm.smarthome.AgentBase;
import es.upm.smarthome.AgentModel;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DecisionAgent extends AgentBase {
    private AID uiAgentAID;
    private Map<String, Double> latestData = new HashMap<>();
    private String lastAction = "Ninguna";

    protected void setup() {
        super.setup();
        this.type = AgentModel.DECISION;
        registerAgentDF();

        // Buscar al agente UI en el DF (se puede hacer al inicio)
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                if (uiAgentAID == null) {
                    var results = getAgentsDF(AgentModel.UI);
                    if (results.length > 0) {
                        uiAgentAID = results[0].getName();
                        log("UI Agent found: " + uiAgentAID.getName());
                    }
                }
                block();
            }
        });

        // Comportamiento para recibir mensajes de sensores (filtro por ontología)
        addBehaviour(new CyclicBehaviour() {
            private MessageTemplate template = MessageTemplate.MatchOntology("weather-ontology");

            public void action() {
                ACLMessage msg = receive(template);
                if (msg != null) {
                    processSensorData(msg);
                } else {
                    block(); // Bloquea hasta que llegue un mensaje con esa ontología
                }
            }
        });
    }

    private void processSensorData(ACLMessage msg) {
        String content = msg.getContent();
        log("Received: " + content);
        // Parsear contenido: "TEMPERATURE:21.5", "HUMIDITY:65.2", etc.
        String[] parts = content.split(":");
        if (parts.length == 2) {
            String type = parts[0];
            String value = parts[1];
            switch (type) {
                case "TEMPERATURE":
                    latestData.put("temp", Double.parseDouble(value));
                    break;
                case "HUMIDITY":
                    latestData.put("hum", Double.parseDouble(value));
                    break;
                case "WIND":
                    latestData.put("wind", Double.parseDouble(value));
                    break;
                case "RAIN":
                    latestData.put("rain", value.equals("true") ? 1.0 : 0.0);
                    break;
            }
            // Cada vez que se recibe un nuevo dato, evaluamos reglas
            evaluateAndDecide();
        }
    }

    private void evaluateAndDecide() {
        if (!latestData.containsKey("temp") || !latestData.containsKey("hum") ||
            !latestData.containsKey("wind") || !latestData.containsKey("rain")) {
            return; // aún no tenemos todos los datos necesarios
        }

        double temp = latestData.get("temp");
        double hum = latestData.get("hum");
        double wind = latestData.get("wind");
        boolean raining = latestData.get("rain") == 1.0;

        StringBuilder actionMsg = new StringBuilder("ACCION:");

        // Reglas de decisión
        if (temp < 20.0) {
            actionMsg.append("Calefaccion_ON;AireAcondicionado_OFF;");
        } else if (temp > 20.0) {
            actionMsg.append("Calefaccion_OFF;AireAcondicionado_ON;");
        } else {
            actionMsg.append("Calefaccion_OFF;");
        }

        if (hum > 60) {
            actionMsg.append("Ventilacion_ON;");
        } else if (hum < 60 && !raining) {
            actionMsg.append("Ventilacion_OFF;Riego_ON;");
        } else {
            actionMsg.append("Ventilacion_OFF;Riego_OFF;");
        }

        if (wind > 15 || raining) {
            actionMsg.append("Persiana_CERRADA;");
        } else {
            actionMsg.append("Persiana_ABIERTA;");
        }

        lastAction = actionMsg.toString();
        log("Decision: " + lastAction);

        // Enviar la acción al agente UI
        if (uiAgentAID != null) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(uiAgentAID);
            msg.setOntology("action-ontology");
            String msgTotal = buildUIMessage();
            msg.setContent(msgTotal);
            send(msg);
        } else {
            log("UI agent not available, cannot send action");
        }
    }
    private String buildUIMessage() {
        double temp = latestData.get("temp");
        double hum = latestData.get("hum");
        double wind = latestData.get("wind");
        boolean rain = latestData.get("rain") == 1.0;
        String sensorPart = String.format(Locale.US, "%.1f;%.1f;%.1f;%s", 
                                           temp, hum, wind, rain ? "si" : "no");
        return sensorPart + "|" + lastAction;
    }
}