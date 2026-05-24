package es.upm.smarthome;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import es.upm.smarthome.agents.*;

public class Main {
    private static AgentContainer cc;

    private static void loadBoot() {
        jade.core.Runtime rt = jade.core.Runtime.instance();
        rt.setCloseVM(true);
        System.out.println("Runtime created");

        Profile profile = new ProfileImpl(null, 1200, null);
        System.out.println("Profile created");

        cc = rt.createMainContainer(profile);
        System.out.println("Main container created");

        try {
            // Lanzar RMA (interfaz gráfica de JADE)
            cc.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]).start();

            // Crear agentes del sistema
            cc.createNewAgent("TempSensor", TemperatureSensorAgent.class.getName(), new Object[]{}).start();
            cc.createNewAgent("HumiditySensor", HumiditySensorAgent.class.getName(), new Object[]{}).start();
            cc.createNewAgent("WindSensor", WindSensorAgent.class.getName(), new Object[]{}).start();
            cc.createNewAgent("RainSensor", RainSensorAgent.class.getName(), new Object[]{}).start();
            cc.createNewAgent("DecisionMaker", DecisionAgent.class.getName(), new Object[]{}).start();
            cc.createNewAgent("UI", UIAgent.class.getName(), new Object[]{}).start();

            System.out.println("All agents launched");
        } catch (StaleProxyException e) {
            System.err.println("Error launching agents");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting Smart Home MAS...");
        loadBoot();
        System.out.println("System ready");
    }
}