package es.upm.smarthome.agents;

import es.upm.smarthome.AgentBase;
import es.upm.smarthome.AgentModel;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import java.util.Random;
import java.util.Locale;

public class HumiditySensorAgent extends AgentBase {
    private AID decisionAgentAID;
    private Random rand = new Random();
    private double currentHumidity = 60.0; // %

    protected void setup() {
        super.setup();
        this.type = AgentModel.HUMIDITY_SENSOR;
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

        addBehaviour(new TickerBehaviour(this, 15000) {
            protected void onTick() {
                if (decisionAgentAID != null) {
                    currentHumidity += (rand.nextDouble() - 0.5) * 10.0;
                    currentHumidity = Math.min(100, Math.max(0, currentHumidity));
                    String content = "HUMIDITY:" + String.format(Locale.US, "%.1f", currentHumidity);
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