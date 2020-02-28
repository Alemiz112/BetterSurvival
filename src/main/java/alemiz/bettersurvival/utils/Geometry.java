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
