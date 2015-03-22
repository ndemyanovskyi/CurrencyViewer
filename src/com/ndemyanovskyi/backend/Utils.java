/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.backend;

/**
 *
 * @author Назарій
 */
class Utils {
    
    public static Float round(Float value) {
        String str = String.valueOf(value);
        int dot = str.indexOf(".");
        if(dot < 0) return value;
        int index = str.substring(dot).lastIndexOf("000");
        if(index < 0) return value;
        return Float.valueOf(str.substring(0, index + dot));
    }
    
}
