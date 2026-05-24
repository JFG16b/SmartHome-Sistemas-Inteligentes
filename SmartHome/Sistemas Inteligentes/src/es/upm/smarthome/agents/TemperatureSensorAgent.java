package es.upm.smarthome.agents;

import es.upm.smarthome.AgentBase;
import es.upm.smarthome.AgentModel;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import java.util.Random;
import java.util.Locale;

public class TemperatureSensorAgent extends AgentBase {
    private static final long serialVersionUID = 1L;
    private AID decisionAgentAID;
    private Random rand = new Random();
    private double currentTemp = 20.0; // ºC

    protected void setup() {
        super.setup();
        this.type = AgentModel.TEMPERATURE_SENSOR;
        registerAgentDF();

        // Buscar al agente Decision en el DF cada cierto tiempo (una vez al inicio)
        addBehaviour(new TickerBehaviour(this, 5000) { // cada 5 seg intenta encontrarlo
            protected void onTick() {
                if (decisionAgentAID == null) {
                    DFAgentDescription[] results = getAgentsDF(AgentModel.DECISION);
                    if (results.length > 0) {
                        decisionAgentAID = results[0].getName();
                        log("Decision agent found: " + decisionAgentAID.getName());
                    }
                }
            }
        });

        // Comportamiento para enviar temperatura cada 10 segundos
        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                if (decisionAgentAID != null) {
                    // Simular cambio de temperatura (±2 grados)
                    currentTemp += (rand.nextDouble() - 0.5) * 4.0;
                    currentTemp = Math.min(40, Math.max(-5, currentTemp));
                    String content = "TEMPERATURE:" + String.format(Locale.US, "%.1f", currentTemp);
                    sendMessage(content);
                } else {
                    log("Decision agent not yet found, retrying...");
                }
            }
        });
    }

    private void sendMessage(String content) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(decisionAgentAID);
        msg.setOntology("weather-ontology");
        msg.setContent(content);
        send(msg);
        log("Sent: " + content);
    }
}