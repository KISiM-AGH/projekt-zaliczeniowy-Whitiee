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

    public byte stateRekrystalizacja;
    public byte getStateRekrystalizacja(){
        return stateRekrystalizacja;
    }
    public void setStateRekrystalizacja(byte stateRekrystalizacja){
        this.stateRekrystalizacja = stateRekrystalizacja;
    }

    public static class State {
        public static byte PUSTE = 0;
        public static byte ZIARNO = 1;
    }

    public static class StateRekrystalizacja {
        public static byte  NIEZREKRYSTALIZOWANY= 0;
        public static byte ZREKRYSTALIZOWANY = 1;
    }

    public enum Dostępność {
        DOSTĘPNE, NIEDOSTĘPNE
    }

    private int id;
    private double gestoscDyslokacji;
    private Dostępność dostępność;
    public int liczbaZrekrystalizowanychSasiadow;

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
        gestoscDyslokacji=0;
        stateRekrystalizacja = StateRekrystalizacja.NIEZREKRYSTALIZOWANY;
        liczbaZrekrystalizowanychSasiadow=0;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getGestoscDyslokacji() {
        return gestoscDyslokacji;
    }

    public void setGestoscDyslokacji(double gestoscDyslokacji) {
        this.gestoscDyslokacji = gestoscDyslokacji;
    }

    public Dostępność getDostępność() {
        return dostępność;
    }

    public void setDostępność(Dostępność dostępność) {
        this.dostępność = dostępność;
    }
}
