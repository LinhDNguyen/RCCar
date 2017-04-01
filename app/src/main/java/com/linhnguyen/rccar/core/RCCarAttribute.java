package com.linhnguyen.rccar.core;

/**
 * Created by linhn on 3/28/17.
 */

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class RCCarAttribute {
    private static HashMap<String, String> attributes = new HashMap();
    public static String CAR_SERVICE_UUID = "12345678-1234-5678-1234-56789abcdef0";
    public static String CAR_MOVE_CHARACTERISTIC_CONFIG = "12345678-1234-5678-1234-56789abcdef1";
    public static String CAR_SOUND_CHARACTERISTIC_CONFIG = "12345678-1234-5678-1234-56789abcdef2";

    static {
        // Sample Services.
        attributes.put("12345678-1234-5678-1234-56789abcdef0", "RC Car Control Service");
        // Sample Characteristics.
        attributes.put(CAR_MOVE_CHARACTERISTIC_CONFIG, "Car Move");
        attributes.put(CAR_SOUND_CHARACTERISTIC_CONFIG, "Car Sound");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}

