/* 
 * Copyright (C) 2016 Tiago Novo <tmnovo at ua.pt>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ua.ri;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tiago Novo <tmnovo at ua.pt>
 */
public class NewMain {

    private static final Logger logger = LoggerFactory.getLogger(NewMain.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        Path base = Paths.get("C:\\Users\\Tiago\\Documents\\NetBeansProjects\\JavaApplication16");
        List<String> gold;
        List<String> normal, proximity;
        float mapN = 0.0f;
        float mapP = 0.0f;
        for (int j = 1; j <= 5; j++) {
            gold = new ArrayList<>(Files.readAllLines(base.resolve("Q" + j + "_gold.txt")));
            normal = new ArrayList<>(Files.readAllLines(base.resolve("Q" + j + ".txt")));
            proximity = new ArrayList<>(Files.readAllLines(base.resolve("Q" + j + "_Proximity.txt")));

            float nprecision = 0.0f;
            int relevant = 0;
            for (int i = 0; i < normal.size(); i++) {
                if (gold.contains(normal.get(i))) {
                    relevant++;
                }
                float precision = (float) relevant / (i + 1);
                if (gold.contains(normal.get(i))) {
                    nprecision += precision;
                }
            }
            nprecision /= gold.size();
            logger.info("Q%d AP: %f\n", j, nprecision);
            mapN += nprecision;
            nprecision = 0.0f;
            relevant = 0;
            for (int i = 0; i < proximity.size(); i++) {
                if (gold.contains(proximity.get(i))) {
                    relevant++;
                }
                float precision = (float) relevant / (i + 1);
                if (gold.contains(proximity.get(i))) {
                    nprecision += precision;
                }
            }
            nprecision /= gold.size();
            logger.info("Q%d: Px AP: %f\n", j, nprecision);
            mapP += nprecision;
        }
        logger.info("MAP Normal = %f%nMAP Prox = %f%n", mapN / 5, mapP / 5);
    }
}
