package hanze.nl.bussimulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class Runner {

    private static HashMap<Integer, ArrayList<Bus>> busStart = new HashMap<>();
    private static ArrayList<Bus> actieveBussen = new ArrayList<>();
    private static int interval = 1000;

    private static void addBus(int starttijd, Bus bus) {
        ArrayList<Bus> bussen = new ArrayList<>();
        if (busStart.containsKey(starttijd)) {
            bussen = busStart.get(starttijd);
        }
        bussen.add(bus);
        busStart.put(starttijd, bussen);
        bus.setbusID(starttijd);
    }

    private static int startBussen(int tijd) {
        actieveBussen.addAll(busStart.get(tijd));
        busStart.remove(tijd);
        return (!busStart.isEmpty()) ? Collections.min(busStart.keySet()) : -1;
    }

    public static void moveBussen(int currentTime) {
        Iterator<Bus> itr = actieveBussen.iterator();
        while (itr.hasNext()) {
            Bus bus = itr.next();
            bus.move();
            if (bus.hasReachedLastStop()) {
                bus.sendLastETA(currentTime);
                itr.remove(); // bus has reached the end, remove from bus list
            }
        }
    }

    public static void sendETAs(int currentTime) {
        for (Bus bus : actieveBussen) {
            bus.sendETAs(currentTime);
        }
    }

    public static int initBussen() {
        Lijn[] lijnen = new Lijn[]{
                Lijn.LIJN1, Lijn.LIJN2, Lijn.LIJN3,
                Lijn.LIJN4, Lijn.LIJN5, Lijn.LIJN6,
                Lijn.LIJN7, Lijn.LIJN1, Lijn.LIJN4,
                Lijn.LIJN5
        };
        Bedrijf[] bedrijven = new Bedrijf[]{
                Bedrijf.ARRIVA, Bedrijf.ARRIVA, Bedrijf.ARRIVA,
                Bedrijf.ARRIVA, Bedrijf.FLIXBUS, Bedrijf.QBUZZ,
                Bedrijf.QBUZZ, Bedrijf.ARRIVA, Bedrijf.ARRIVA,
                Bedrijf.FLIXBUS
        };
        int[] times = new int[] {
                3, 3, 4, 4, 5,
                5, 6, 6, 10, 12
        };

        // richting 1
        for (int busID = 0; busID < lijnen.length; busID++) {
            addBus(times[busID], new Bus(lijnen[busID], bedrijven[busID], 1));
        }
        // richting -1
        for (int busID = 0; busID < lijnen.length; busID++) {
            addBus(times[busID], new Bus(lijnen[busID], bedrijven[busID], -1));
        }

        return Collections.min(busStart.keySet());
    }


    public static void main(String[] args) throws InterruptedException {
        int tijd = 0;
        int volgende = initBussen();
        while ((volgende >= 0) || !actieveBussen.isEmpty()) {
            System.out.println("De tijd is:" + tijd);
            volgende = (tijd == volgende) ? startBussen(tijd) : volgende;
            moveBussen(tijd);
            sendETAs(tijd);
            Thread.sleep(interval);
            tijd++;
        }
    }

}
