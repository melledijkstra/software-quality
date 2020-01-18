package hanze.nl.bussimulator;

import hanze.nl.bussimulator.Halte.Positie;

public class Bus {

    private String busID;
    private Bedrijf bedrijf;
    private Lijn lijn;
    private int huidigeHalte;
    private int totVolgendeHalte;
    private int richting;
    private boolean bijHalte;

    Bus(Lijn lijn, Bedrijf bedrijf, int richting) {
        this.lijn = lijn;
        this.bedrijf = bedrijf;
        this.richting = richting;
        this.huidigeHalte = -1;
        this.totVolgendeHalte = 0;
        this.bijHalte = false;
        this.busID = "Niet gestart";
    }

    public void setbusID(int starttijd) {
        this.busID = starttijd + lijn.name() + richting;
    }

    public void naarVolgendeHalte() {
        Positie volgendeHalte = lijn.getHalte(huidigeHalte + richting).getPositie();
        totVolgendeHalte = lijn.getHalte(huidigeHalte).afstand(volgendeHalte);
    }

    public void halteBereikt() {
        huidigeHalte += richting;
        bijHalte = true;
        String msg;
        if (hasReachedLastStop()) {
            msg = "Bus %s heeft eindpunt (halte %s, richting %d) bereikt.%n";
        } else {
            msg = "Bus %s heeft halte %s, richting %d bereikt.%n";
        }
        System.out.printf(msg, lijn.name(), lijn.getHalte(huidigeHalte), lijn.getRichting(huidigeHalte));
    }

    public void start() {
        huidigeHalte = (richting == 1) ? 0 : lijn.getLengte() - 1;
        System.out.printf("Bus %s is vertrokken van halte %s in richting %d.%n",
                lijn.name(), lijn.getHalte(huidigeHalte), lijn.getRichting(huidigeHalte));
    }

    public void move() {
        bijHalte = false;
        if (huidigeHalte == -1) {
            start();
            naarVolgendeHalte();
        } else {
            totVolgendeHalte--;
            if (totVolgendeHalte == 0) {
                halteBereikt();
                if (!hasReachedLastStop()) {
                    naarVolgendeHalte();
                }
            }
        }
    }

    public boolean hasReachedLastStop() {
        return (huidigeHalte >= lijn.getLengte() - 1) || (huidigeHalte == 0);
    }

    public void sendETAs(int currentTime) {
        Bericht bericht = new Bericht(lijn.name(), bedrijf.name(), busID, currentTime);

        if (bijHalte) {
            ETA eta = new ETA(lijn.getHalte(huidigeHalte).name(), lijn.getRichting(huidigeHalte), 0);
            bericht.ETAs.add(eta);
        }

        Positie eerstVolgende = lijn.getHalte(huidigeHalte + richting).getPositie();
        int tijdNaarHalte = totVolgendeHalte + currentTime;
        int etaHalte;

        for (
                etaHalte = huidigeHalte + richting;
                !(etaHalte >= lijn.getLengte()) && !(etaHalte < 0);
                etaHalte = etaHalte + richting
        ) {
            tijdNaarHalte += lijn.getHalte(etaHalte).afstand(eerstVolgende);
            ETA eta = new ETA(lijn.getHalte(etaHalte).name(), lijn.getRichting(etaHalte), tijdNaarHalte);
            bericht.ETAs.add(eta);
            eerstVolgende = lijn.getHalte(etaHalte).getPositie();
        }

        bericht.eindpunt = lijn.getHalte(etaHalte - richting).name();

        sendBericht(bericht);
    }

    public void sendLastETA(int currentTime) {
        Bericht bericht = new Bericht(lijn.name(), bedrijf.name(), busID, currentTime);
        String eindpunt = lijn.getHalte(huidigeHalte).name();
        ETA eta = new ETA(eindpunt, lijn.getRichting(huidigeHalte), 0);
        bericht.ETAs.add(eta);
        bericht.eindpunt = eindpunt;
        sendBericht(bericht);
    }

    public void sendBericht(Bericht bericht) {
        //TODO verstuur een XML bericht naar de messagebroker.
    }
}
