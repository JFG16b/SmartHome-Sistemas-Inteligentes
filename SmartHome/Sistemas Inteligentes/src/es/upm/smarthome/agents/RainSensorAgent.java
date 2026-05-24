package es.upm.smarthome.agents;

import es.upm.smarthome.AgentBase;
import es.upm.smarthome.AgentModel;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import java.util.Random;
import java.util.Locale;

public class RainSensorAgent extends AgentBase {
    private AID decisionAgentAID;
    private Random rand = new Random();
    private boolean raining = false;

    protected void setup() {
        super.setup();
        this.type = AgentModel.RAIN_SENSOR;
        registerAgentDF();

        addBehaviour(new TickerBehaviour(this, 5000) {
            protected void onTick() {
                if (decisionAgentAID == null) {
                    DFAgentDescription[] results = getAgentsDF(AgentModel.DECISION);
                    if (results.length > 0) {
                        decisionAgentAID = results[0].getName();
                    }
                }
            }
        });

        addBehaviour(new TickerBehaviour(this, 30000) {
            protected void onTick() {
                if (decisionAgentAID != null) {
                    // Simular cambio de estado de lluvia (10% de probabilidad de cambiar)
                    if (rand.nextDouble() < 0.5) raining = !raining;
                    String content = "RAIN:" + (raining ? "true" : "false");
                    sendMessage(content);
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