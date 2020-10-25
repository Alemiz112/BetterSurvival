/*
 * Copyright 2020 Alemiz
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package alemiz.bettersurvival.utils;

import cn.nukkit.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class Geometry {

    public static List<Vector3> circle(Vector3 center, double radius, int points){
        List<Vector3> vectors = new ArrayList<>();

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            Vector3 pos = center.clone().add(radius * Math.sin(angle),
                    0,
                    radius * Math.cos(angle));
            vectors.add(pos);
        }

        return vectors;
    }

}
