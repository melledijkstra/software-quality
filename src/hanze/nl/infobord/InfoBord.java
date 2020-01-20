package hanze.nl.infobord;

import org.codehaus.jackson.map.ObjectMapper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
//import hanze.nl.tijdtools.InfobordTijdFuncties;

public class InfoBord {

    private static HashMap<String, Integer> laatsteBericht = new HashMap<String, Integer>();
    private static HashMap<String, JSONBericht> infoBordRegels = new HashMap<String, JSONBericht>();
    private static InfoBord infobord;
    private static int hashTimeValue;
    private JFrame scherm;

    private JLabel[] lineLabels = new JLabel[4];

    private InfoBord() {
        setupLayout();
    }

    public static InfoBord getInfoBord() {
        if (infobord == null) {
            infobord = new InfoBord();
        }
        return infobord;
    }

    public static void verwerktBericht(String incoming) {
        try {
            JSONBericht bericht = new ObjectMapper().readValue(incoming, JSONBericht.class);
            String busID = bericht.getBusID();
            Integer tijd = bericht.getTijd();
            if (!laatsteBericht.containsKey(busID) || laatsteBericht.get(busID) <= tijd) {
                laatsteBericht.put(busID, tijd);
                if (bericht.getAankomsttijd() == 0) {
                    infoBordRegels.remove(busID);
                } else {
                    infoBordRegels.put(busID, bericht);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setupLayout() {
        this.scherm = new JFrame("InfoBord");
        JPanel panel = new JPanel();
        BoxLayout boxlayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(boxlayout);
        panel.setBorder(new EmptyBorder(new Insets(10, 20, 10, 20)));
        JLabel tijdregel1 = new JLabel("Scherm voor de laatste keer bijgewerkt op:");
        JLabel tijdregel2 = new JLabel("00:00:00");
        panel.add(tijdregel1);
        panel.add(tijdregel2);
        for (int i = 0; i < 4; i++) {
            lineLabels[i] = new JLabel(String.format("-regel%d-", i + 1));
            panel.add(lineLabels[i]);
        }
        scherm.add(panel);
        scherm.pack();
        scherm.setVisible(true);
    }

    public void setRegels() {
        String[] infoTekst = {"--1--", "--2--", "--3--", "--4--", "leeg"};
        int[] aankomstTijden = new int[5];
        int aantalRegels = 0;
        if (!infoBordRegels.isEmpty()) {
            for (String busID : infoBordRegels.keySet()) {
                JSONBericht regel = infoBordRegels.get(busID);
                int dezeTijd = regel.getAankomsttijd();
                String dezeTekst = regel.getInfoRegel();
                int plaats = aantalRegels;
                for (int i = aantalRegels; i > 0; i--) {
                    if (dezeTijd < aankomstTijden[i - 1]) {
                        aankomstTijden[i] = aankomstTijden[i - 1];
                        infoTekst[i] = infoTekst[i - 1];
                        plaats = i - 1;
                    }
                }
                aankomstTijden[plaats] = dezeTijd;
                infoTekst[plaats] = dezeTekst;
                if (aantalRegels < 4) {
                    aantalRegels++;
                }
            }
        }
        if (hasToRepaint(aankomstTijden)) {
            repaintInfoBord(infoTekst);
        }
    }

    private boolean hasToRepaint(int[] aankomstTijden) {
        int totaalTijd = 0;
        for (int aankomstTijd : aankomstTijden) {
            totaalTijd += aankomstTijd;
        }
        if (hashTimeValue != totaalTijd) {
            hashTimeValue = totaalTijd;
            return true;
        }
        return false;
    }

    private void repaintInfoBord(String[] lineTexts) {
        for (int i = 0; i < 4; i++) {
            lineLabels[i].setText(lineTexts[i]);
        }
        scherm.repaint();
    }
}
