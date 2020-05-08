package samoth69.plugin;

import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;

public class GameSettings {

    private Main main;

    private int tpsInvincibilite = 2;
    private int tpsPVP = 10;

    private int tpsBordure = 60;
    private int tailleBordure = 500; //ATTENTION: CETTE VALEUR DOIS ÊTRE DOUBLé QUAND MISE DANS Main.wb.setSize()

    private boolean enableTaupe = false;
    private int tpsTaupe = 20;

    private WorldBorder wb = Bukkit.getWorlds().get(0).getWorldBorder();

    public GameSettings(Main main) {
        this.main = main;

        this.wb.setCenter(0,0);
        this.wb.setWarningDistance(16);
        this.wb.setWarningTime(5);
        this.wb.setDamageBuffer(0);
        this.wb.setSize(tailleBordure * 2);
    }

    public int getTpsInvincibilite() {
        return tpsInvincibilite;
    }

    public void setTpsInvincibilite(int tpsInvincibilite) {
        this.tpsInvincibilite = tpsInvincibilite;
    }

    public int getTpsPVP() {
        return tpsPVP;
    }

    public void setTpsPVP(int tpsPVP) {
        this.tpsPVP = tpsPVP;
    }

    public int getTpsBordure() {
        return tpsBordure;
    }

    public void setTpsBordure(int tpsBordure) {
        this.tpsBordure = tpsBordure;
    }

    public int getTailleBordure() {
        return tailleBordure;
    }

    public void setTailleBordure(int tailleBordure) {
        this.tailleBordure = tailleBordure;
    }

    public String getTailleBordureTextFormatted() {
        return (int)(-wb.getSize() / 2) + " / " + (int)(wb.getSize() / 2);
    }

    public int getTpsTaupe() {
        return tpsTaupe;
    }

    public void setTpsTaupe(int tpsTaupe) {
        this.tpsTaupe = tpsTaupe;
    }

    public boolean isEnableTaupe() {
        return enableTaupe;
    }

    public void setEnableTaupe(boolean enableTaupe) {
        this.enableTaupe = enableTaupe;
    }
}
