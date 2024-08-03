package de.terranova.nations.worldguard.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class claimCalc {

    static boolean debug = false;

    public static Optional<List<Vectore2>> dothatshitforme(List<Vectore2> oldlist, List<Vectore2> newlist) {

        if (debug) {
            System.out.println(".");
            System.out.println("OLDLIST");
            for (Vectore2 v : oldlist) {
                System.out.println("x: " + v.x + ", z: " + v.z);
            }
            System.out.println("NEWLIST");
            for (Vectore2 v : newlist) {
                System.out.println("x: " + v.x + ", z " + v.z);
            }
        }

        List<Vectore2> normedold = normalisieren(oldlist);

        if (debug) {
            System.out.println("OLD NORMED");
            for (Vectore2 v : normedold) {
                System.out.println("x: " + v.x + ", z: " + v.z);
            }
        }

        List<Vectore2> oldaufplustern = aufplustern(normedold);
        List<Vectore2> newaufplustern = aufplustern(newlist);

        if (debug) {
            System.out.println("ALT PLUSTERED");
            for (Vectore2 v : oldaufplustern) {
                System.out.println("x: " + v.x + ", z: " + v.z);
            }
            System.out.println("NEW PLUSTERED");
            for (Vectore2 v : newaufplustern) {
                System.out.println("x: " + v.x + ", z " + v.z);
            }
        }

        List<Vectore2> oldproj = projezieren(oldaufplustern);
        List<Vectore2> newproj = projezieren(newaufplustern);

        if (debug) {
            System.out.println("ALT PROJ");
            for (Vectore2 v : oldproj) {
                System.out.println("x: " + v.x + ", z: " + v.z);
            }
            System.out.println("NEW PROJ");
            for (Vectore2 v : newproj) {
                System.out.println("x: " + v.x + ", z " + v.z);
            }
        }

        Optional<List<Vectore2>> merged = mergen(oldproj, newproj);

        if (merged.isEmpty()) {
            return Optional.empty();
        }

        if (debug) {
            System.out.println("MERGED");
            for (Vectore2 v : merged.get()) {
                System.out.println("x: " + v.x + ", z " + v.z);
            }
        }

        List<Vectore2> entproj = entprojezieren(merged.get());

        if (debug) {
            System.out.println("ENTPROJ");
            for (Vectore2 v : entproj) {
                System.out.println("x: " + v.x + ", z " + v.z);
            }
        }

        List<Vectore2> entplustern = reverseaufplustern(entproj);

        if (debug) {
            System.out.println("ENTPLUSTERT");
            for (Vectore2 v : entplustern) {
                System.out.println("x: " + v.x + ", z " + v.z);
            }
        }

        List<Vectore2> entnormalisieren = entnormalisieren(entplustern);

        if (debug) {
            System.out.println("ENTNORMALISIEREN");
            for (Vectore2 v : entnormalisieren) {
                System.out.println("x: " + v.x + ", z " + v.z);
            }
        }
        return Optional.of(entnormalisieren);

    }

    public static double area(Vectore2[] vertices)
    {
        double sum = 0;
        for (int i = 0; i < vertices.length ; i++)
        {
            if (i == 0)
            {
                //System.out.println(vertices[i].x + "x" + (vertices[i + 1].z + "-" + vertices[vertices.length - 1].z));
                sum += vertices[i].x * (vertices[i + 1].z - vertices[vertices.length - 1].z);
            }
            else if (i == vertices.length - 1)
            {
                //System.out.println(vertices[i].x + "x" + (vertices[0].z + "-" + vertices[i - 1].z));
                sum += vertices[i].x * (vertices[0].z - vertices[i - 1].z);
            }
            else
            {
                //System.out.println(vertices[i].x + "x" + (vertices[i + 1].z + "-" + vertices[i - 1].z));
                sum += vertices[i].x * (vertices[i + 1].z - vertices[i - 1].z);
            }
        }

        double area = 0.5 * Math.abs(sum);
        return area;

    }

    public static List<Vectore2> normalisieren(List<Vectore2> current) {

        List<Vectore2> output = new ArrayList<>();

        for (Vectore2 v : current) {
            output.add(new Vectore2(v.x + 0.5, v.z + 0.5));
        }


        return output;
    }

    static List<Vectore2> entnormalisieren(List<Vectore2> current) {

        List<Vectore2> output = new ArrayList<>();

        for (Vectore2 v : current) {
            output.add(new Vectore2(v.x - 0.5, v.z - 0.5));
        }
        return output;
    }

    static Optional<List<Vectore2>> mergen(List<Vectore2> oldRegion, List<Vectore2> newRegion) {

        List<Vectore2> output = new ArrayList<>();

        List<Vectore2> oldr = oldRegion;
        List<Vectore2> newr = newRegion;

        List<MarkedVectore2> markedOld = new ArrayList<>();
        List<Vectore2> newk = newRegion;


        //MARKIEREN DER GLEICHEN WERTE
        boolean nomark = true;
        for (int i = 0; i < oldr.size(); i++) {
            for (int j = 0; j < newr.size(); j++) {
                if (oldr.get(i).z == newr.get(j).z && oldr.get(i).x == newr.get(j).x) {
                    markedOld.add(new MarkedVectore2(oldr.get(i), true));
                    //newk.remove(j);
                    nomark = false;
                }
            }
            if (nomark) {
                markedOld.add(new MarkedVectore2(oldr.get(i), false));
            }
            nomark = true;
        }


        MarkedVectore2 last2 = markedOld.getLast();
        MarkedVectore2 current2;
        MarkedVectore2 next2;
        int k = 0;

        //ENTFERNEN DER DOPPELTEN ECKEN AUS newk
        for (MarkedVectore2 v : markedOld) {


            current2 = markedOld.get(k);
            if (k == markedOld.size() - 1) {
                k = -1;
            }
            next2 = markedOld.get(k + 1);
            if (current2.bool) {
                if ((next2.bool || last2.bool)) {
                    if (v.v2.equals(current2.v2)) {

                        for (int o = 0; o < newk.size(); o++) {
                            if (newk.get(o).equals(current2.v2)) {
                                newk.remove(o);
                            }
                        }
                    }
                }

            }
            last2 = current2;
            k++;

        }

        // BETRACHTUNG

        int marker = -1;
        int index = 0;

        MarkedVectore2 last = markedOld.getLast();
        MarkedVectore2 current;
        MarkedVectore2 next;
        int pairs = 0;

        for (MarkedVectore2 v : markedOld) {


            current = v;
            index++;
            if (index >= markedOld.size()) {
                index = 0;
            }

            next = markedOld.get(index);


            if (current.bool) {

                if (!last.bool || !next.bool) {
                    pairs++;
                }

                if (!last.bool && next.bool) {
                    marker = index;
                }
            }

            //xon
            if (last.bool) {
                if (current.bool) {
                    //xxx
                    if (next.bool) {
                        last = current;
                        continue;
                    }
                }
                output.add(current.v2);
            } else {
                //ooo
                output.add(current.v2);
            }


            last = current;
        }

        if (pairs >= 4) return Optional.empty();

        if (marker >= 0 && !newRegion.isEmpty()) {
            if (newRegion.size() == 2 && (newRegion.get(0).x == newRegion.get(1).x)) {

                double abstand;
                if (marker == 0) {
                    abstand = abstand(output.getLast(), newRegion.getFirst());
                    //abstand = (Math.sqrt((Math.pow((output.getLast().x - newRegion.getFirst().x), 2) + Math.pow((output.getLast().z - newRegion.getFirst().z), 2))));
                } else {
                    abstand = abstand(output.get(marker - 1), newRegion.getFirst());
                    //abstand = (Math.sqrt((Math.pow((output.get(marker - 1).x - newRegion.getFirst().x), 2) + Math.pow((output.get(marker - 1).z - newRegion.getFirst().z), 2))));
                }
                if (!(abstand == 48)) {
                    Collections.rotate(newRegion, 1);
                }

            }

            Collections.rotate(output, output.size() - marker);
            output.addAll(newRegion);
            Collections.rotate(output, marker + newRegion.size());
        }


        return Optional.of(output);


    }

    public static double abstand(Vectore2 a, Vectore2 b) {
        return Math.sqrt((Math.pow(a.x - b.x, 2) + Math.pow(a.z - b.z, 2)));
    }

    static List<Vectore2> projezieren(List<Vectore2> current) {

        List<Vectore2> output = new ArrayList<>();

        int index = 1;
        Vectore2 next;

        for (Vectore2 list : current) {

            if (index == current.size()) {
                index = 0;
            }

            next = current.get(index);


            if (list.z == next.z) {


                if (list.x > next.x) {
                    if (Math.abs(next.x - list.x) == 48) {
                        output.add(new Vectore2(list.x, list.z));
                        index++;
                        continue;
                    }
                    output.add(new Vectore2(list.x, list.z));
                    for (int i = 0; i < Math.abs(next.x - list.x) / 48 - 1; i++) {

                        output.add(new Vectore2(list.x - 48 * (i + 1), list.z));

                    }
                    index++;
                }
                if (list.x < next.x) {
                    if (Math.abs(next.x - list.x) == 48) {
                        output.add(new Vectore2(list.x, list.z));
                        index++;
                        continue;
                    }
                    output.add(new Vectore2(list.x, list.z));
                    for (int i = 0; i < Math.abs(list.x - next.x) / 48 - 1; i++) {
                        output.add(new Vectore2(list.x + 48 * (i + 1), list.z));
                    }

                    index++;
                }


            }

            if (list.x == next.x) {


                if (list.z > next.z) {
                    if (Math.abs(next.z - list.z) == 48) {
                        output.add(new Vectore2(list.x, list.z));
                        index++;
                        continue;
                    }
                    output.add(new Vectore2(list.x, list.z));
                    for (int i = 0; i < Math.abs(next.z - list.z) / 48 - 1; i++) {

                        output.add(new Vectore2(list.x, list.z - 48 * (i + 1)));

                    }
                    index++;
                }
                if (list.z < next.z) {
                    if (Math.abs(next.z - list.z) == 48) {
                        output.add(new Vectore2(list.x, list.z));
                        index++;
                        continue;
                    }
                    output.add(new Vectore2(list.x, list.z));
                    for (int i = 0; i < Math.abs(list.z - next.z) / 48 - 1; i++) {

                        output.add(new Vectore2(list.x, list.z + 48 * (i + 1)));

                    }
                    index++;
                }


            }


        }

        return output;

    }

    static List<Vectore2> entprojezieren(List<Vectore2> current) {

        List<Vectore2> output = new ArrayList<>();

        Vectore2 last = current.getLast();
        Vectore2 next;
        int i = 0;

        for (Vectore2 list : current) {

            if (i == current.size() - 1) {
                i = 0;
            }

            next = current.get(i + 1);

            if (list.z == last.z) {
                if (list.z == next.z) {
                    i++;
                    continue;
                }
            }
            if (list.x == last.x) {
                if (list.x == next.x) {
                    i++;
                    continue;
                }
            }
            output.add(list);
            last = list;
            i++;
        }

        return output;

    }

    public static List<Vectore2> aufplustern(List<Vectore2> current) {

        List<Vectore2> output = new ArrayList<>();

        boolean firstValue = true;
        int index = 1;
        Vectore2 last = current.getLast();
        Vectore2 next = current.get(1);


        for (Vectore2 list : current) {


            if (index == current.size() - 1) {
                index = 0;
            }

            if (last.z == list.z) {
                //Bewegung auf der Z Achse (= bei x erste spalte und = bei z zweite spalte)
                if (list.x > last.x) {
                    //Bewegung in den + Bereich (+ z erste spalte)
                    if (list.z < next.z) {
                        //Bewegung in den + Bereich (+ x zweite spalte)
                        output.add(new Vectore2(list.x + 0.5f, list.z - 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                    if (list.z > next.z) {
                        //Bewegung in den - Bereich (- x zweite spalte)
                        output.add(new Vectore2(list.x - 0.5f, list.z - 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                }
                if (list.x < last.x) {
                    //Bewegung in den - Bereich (- z erste spalte)
                    if (list.z < next.z) {
                        //Bewegung in den + Bereich (+ x zweite spalte)
                        output.add(new Vectore2(list.x + 0.5f, list.z + 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                    if (list.z > next.z) {
                        //Bewegung in den - Bereich (- x zweite spalte)
                        output.add(new Vectore2(list.x - 0.5f, list.z + 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                }
            }
            if (last.x == list.x) {
                //Bewegung auf der X Achse (= bei z erste spalte und = bei x zweite spalte)
                if (list.z > last.z) {
                    //Bewegung in den + Bereich (+ x erste spalte)
                    if (list.x < next.x) {
                        //Bewegung in den + Bereich (+ z zweite spalte)
                        output.add(new Vectore2(list.x + 0.5f, list.z - 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                    if (list.x > next.x) {
                        //Bewegung in den - Bereich (- z zweite spalte)
                        output.add(new Vectore2(list.x + 0.5f, list.z + 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                }
                if (list.z < last.z) {
                    //Bewegung in den - Bereich (- x erste spalte)
                    if (list.x < next.x) {
                        //Bewegung in den + Bereich (+ z zweite spalte)
                        output.add(new Vectore2(list.x - 0.5f, list.z - 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);
                        continue;
                    }
                    if (list.x > next.x) {
                        //Bewegung in den - Bereich (- z zweite spalte)
                        output.add(new Vectore2(list.x - 0.5f, list.z + 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);
                        continue;
                    }
                }
            }
        }
        return output;
    }

    static List<Vectore2> reverseaufplustern(List<Vectore2> current) {

        List<Vectore2> output = new ArrayList<>();

        boolean firstValue = true;
        int index = 1;
        Vectore2 last = current.getLast();
        Vectore2 next = current.get(1);


        for (Vectore2 list : current) {


            if (index == current.size() - 1) {
                index = 0;
            }


            if (last.z == list.z) {
                //Bewegung auf der Z Achse (= bei x erste spalte und = bei z zweite spalte)
                if (list.x > last.x) {
                    //Bewegung in den + Bereich (+ z erste spalte)
                    if (list.z < next.z) {
                        //Bewegung in den + Bereich (+ x zweite spalte)
                        output.add(new Vectore2(list.x - 0.5f, list.z + 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                    if (list.z > next.z) {
                        //Bewegung in den - Bereich (- x zweite spalte)
                        output.add(new Vectore2(list.x + 0.5f, list.z + 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                }
                if (list.x < last.x) {
                    //Bewegung in den - Bereich (- z erste spalte)
                    if (list.z < next.z) {
                        //Bewegung in den + Bereich (+ x zweite spalte)
                        output.add(new Vectore2(list.x - 0.5f, list.z - 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                    if (list.z > next.z) {
                        //Bewegung in den - Bereich (- x zweite spalte)
                        output.add(new Vectore2(list.x + 0.5f, list.z - 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                }
            }
            if (last.x == list.x) {
                //Bewegung auf der X Achse (= bei z erste spalte und = bei x zweite spalte)
                if (list.z > last.z) {
                    //Bewegung in den + Bereich (+ x erste spalte)
                    if (list.x < next.x) {
                        //Bewegung in den + Bereich (+ z zweite spalte)
                        output.add(new Vectore2(list.x - 0.5f, list.z + 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                    if (list.x > next.x) {
                        //Bewegung in den - Bereich (- z zweite spalte)
                        output.add(new Vectore2(list.x - 0.5f, list.z - 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                }
                if (list.z < last.z) {
                    //Bewegung in den - Bereich (- x erste spalte)
                    if (list.x < next.x) {
                        //Bewegung in den + Bereich (+ z zweite spalte)
                        output.add(new Vectore2(list.x + 0.5f, list.z + 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);
                        continue;
                    }
                    if (list.x > next.x) {
                        //Bewegung in den - Bereich (- z zweite spalte)
                        output.add(new Vectore2(list.x + 0.5f, list.z - 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);
                        continue;
                    }
                }
            }
        }
        return output;
    }

}
