package com.example.junseo.test03.arduino;

import android.util.Log;

import com.example.junseo.test03.arduino.PacketParser.Type;

public class Packet<T> {
    public Type type = Type.Error;
    public int id = -1;
    public T data = null;

    public Packet(Type type) {
        this.type = type;
    }

    public Packet(Type type, T data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public String toString() {
        Log.d("packet", Packet.class.getName() + ": " + id + ": "+ type.toString() + ": " + data);
        return Packet.class.getName() + ": " + id + ": "+ type.toString() + ": " + data;
    }
}
