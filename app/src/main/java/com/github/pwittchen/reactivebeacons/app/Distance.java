package com.github.pwittchen.reactivebeacons.app;

/**
 * Created by sumitm on 07-Apr-17.
 */

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Distance implements Comparable<String> {

    private String name;
    private String phone;

    public int compareTo(String other) {
        return name.compareTo(other);
    }
}