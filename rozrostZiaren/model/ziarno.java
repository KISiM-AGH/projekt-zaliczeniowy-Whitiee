package model;
import static model.ziarno.Dostępność.*;

public class ziarno {
    public byte state;
    public byte getState(){
        return state;
    }
    public void setState(byte state){
        this.state = state;
    }

    public static class State {
        public static byte PUSTE = 0;
        public static byte ZIARNO = 1;
    }

    public enum Dostępność {
        DOSTĘPNE, NIEDOSTĘPNE
    }

    private int id;
    private Dostępność dostępność;

    public int getEnergia() {
        return energia;
    }

    public void setEnergia(int energia) {
        this.energia = energia;
    }

    private int energia;

    public ziarno() {
        state = State.PUSTE;
        dostępność = DOSTĘPNE;
        id = 0;
        energia = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Dostępność getDostępność() {
        return dostępność;
    }

    public void setDostępność(Dostępność dostępność) {
        this.dostępność = dostępność;
    }
}
