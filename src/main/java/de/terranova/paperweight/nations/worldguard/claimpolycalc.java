package de.terranova.paperweight.nations.worldguard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class claimpolycalc {
    /*
    public static void main(String[] args) {
        List<Vector2> boss = new ArrayList<>();
        List<Vector2> threesides = new ArrayList<>();
        List<Vector2> corner = new ArrayList<>();
        List<Vector2> circle = new ArrayList<>();
        List<Vector2> onechunk = new ArrayList<>();
        List<Vector2> onechunkr = new ArrayList<>();

        List<Vector2> boss2 = new ArrayList<>();
        List<Vector2> strange = new ArrayList<>();

        boss.add(new Vector2(0.5f, 48.5f));
        boss.add(new Vector2(48.5f, 48.5f));
        boss.add(new Vector2(48.5f, -0.5));
        boss.add(new Vector2(0.5f, -0.5));
        boss.add(new Vector2(0.5f, -47.5));
        boss.add(new Vector2(191.5f, -47.5));
        boss.add(new Vector2(191.5f, -0.5));
        boss.add(new Vector2(95.5f, -0.5));
        boss.add(new Vector2(95.5f, 48.5f));
        boss.add(new Vector2(191.5f, 48.5f));
        boss.add(new Vector2(191.5f, 95.5f));
        boss.add(new Vector2(95.5f, 95.5f));
        boss.add(new Vector2(95.5f, 143.5f));
        boss.add(new Vector2(48.5f, 143.5f));
        boss.add(new Vector2(48.5f, 95.5f));
        boss.add(new Vector2(0.5f, 95.5f));

        onechunk.add(new Vector2(144.5, 96.5f));
        onechunk.add(new Vector2(191.5f, 96.5f));
        onechunk.add(new Vector2(191.5f, 143.5f));
        onechunk.add(new Vector2(144.5f, 143.5f));

        onechunkr.add(new Vector2(-47.5, 48.5));
        onechunkr.add(new Vector2(-0.5, 48.5));
        onechunkr.add(new Vector2(-0.5, 95.5));
        onechunkr.add(new Vector2(-47.5, 95.5));

        threesides.add(new Vector2(0.5, 47.5));
        threesides.add(new Vector2(0.5, 0.5));
        threesides.add(new Vector2(47.5, 0.5));
        threesides.add(new Vector2(47.5, 47.5));

        corner.add(new Vector2(0.5, 96.5));
        corner.add(new Vector2(47.5, 96.5));
        corner.add(new Vector2(47.5, 143.5));
        corner.add(new Vector2(0.5, 143.5));

        circle.add(new Vector2(144.5, 0.5));
        circle.add(new Vector2(191.5, 0.5));
        circle.add(new Vector2(191.5, 47.5));
        circle.add(new Vector2(144.5, 47.5));

        boss2.add(new Vector2(0.5f, 48.5f));
        boss2.add(new Vector2(48.5f, 48.5f));
        boss2.add(new Vector2(48.5f, -0.5));
        boss2.add(new Vector2(0.5f, -0.5));
        boss2.add(new Vector2(0.5f, -47.5));
        boss2.add(new Vector2(191.5f, -47.5));
        boss2.add(new Vector2(191.5f, -0.5));
        boss2.add(new Vector2(95.5f, -0.5));
        boss2.add(new Vector2(95.5f, 48.5f));
        boss2.add(new Vector2(143.5f, 48.5f));
        boss2.add(new Vector2(143.5f, 95.5f));
        boss2.add(new Vector2(95.5f, 95.5f));
        boss2.add(new Vector2(95.5f, 143.5f));
        boss2.add(new Vector2(48.5f, 143.5f));
        boss2.add(new Vector2(48.5f, 95.5f));
        boss2.add(new Vector2(0.5f, 95.5f));

        strange.add(new Vector2(144.5, 0.5));
        strange.add(new Vector2(191.5, 0.5));
        strange.add(new Vector2(191.5, 47.5));
        strange.add(new Vector2(144.5, 47.5));


        List<Vector2> outputplust = aufplustern(boss);
        List<Vector2> output2plust = aufplustern(onechunk);


        System.out.println("------1:Aufplustern------");
        System.out.println("Size: " + outputplust.size());
        for (Vector2 v : outputplust) {
            System.out.println("Ergebnis: z:" + v.z + " x:" + v.x);

        }
        List<Vector2> outputproj = projezieren(outputplust);
        List<Vector2> output2proj = projezieren(output2plust);

        System.out.println("------2:Projezieren A------");
        System.out.println("Size: " + outputproj.size());
        for (Vector2 v : outputproj) {
            System.out.println("Ergebnis: z:" + v.z + " x:" + v.x);
        }
        System.out.println("------2:Projezieren B------");
        for (Vector2 v : output2proj) {
            System.out.println("Ergebnis: z:" + v.z + " x:" + v.x);
        }


        Optional<List<Vector2>> outputsmergedoptional = mergen(outputproj, output2proj);
        List<Vector2> outputsmerged = new ArrayList<>();

        if (outputsmergedoptional.isPresent()) {
            outputsmerged = outputsmergedoptional.get();
        } else {
            System.out.println("Dein Claim ist so leider nicht möglich!");
        }

        System.out.println("------3:Mergen------");
        System.out.println("Size: " + outputsmerged.size());

        for (Vector2 v : outputsmerged) {
            System.out.println("Ergebnis: z:" + v.z + " x:" + v.x);
        }

        List<Vector2> entproj = entprojezieren(outputsmerged);
        System.out.println("------4:Entprojezieren------");
        System.out.println("Size: " + entproj.size());

        for (Vector2 v : entproj) {
            System.out.println("Ergebnis: z:" + v.z + " x:" + v.x);
        }

        List<Vector2> rauf = reverseaufplustern(entproj);
        System.out.println("------4:Entprojezieren------");
        System.out.println("Size: " + rauf.size());

        for (Vector2 v : rauf) {
            System.out.println("Ergebnis: z:" + v.z + " x:" + v.x);
        }



    }


*/

    /* gibt dir die unterste -z -x ecke aus
    static List<Vector2> ecke(List<Vector2> current) {


        List<Vector2> output = current;
        int index = 0;
        double lowestvalue = 0;
        boolean first = true;

        for(int i = 0; i<current.size(); i++){
            if(first) {
                lowestvalue = current.get(i).z + current.get(i).x;
                first = false;
                continue;
            }
            if((current.get(i).z + current.get(i).x) < lowestvalue){
                lowestvalue = current.get(i).z + current.get(i).x;
                index = i;
            }
        }

        Collections.rotate(output,current.size() - index);

        return output;
    }

     */

    public static Optional<List<Vectore2>> mergen(List<Vectore2> oldRegion, List<Vectore2> newRegion) {
        List<Vectore2> output = new ArrayList<>();

        List<Vectore2> oldr = oldRegion;
        List<Vectore2> newr = newRegion;

        List<MarkedVector2> markedOld = new ArrayList<>();
        List<Vectore2> newk = newRegion;


        //MARKIEREN DER GLEICHEN WERTE
        boolean nomark = true;
        for (int i = 0; i < oldr.size(); i++) {
            for (int j = 0; j < newr.size(); j++) {
                if (oldr.get(i).x == newr.get(j).x && oldr.get(i).z == newr.get(j).z) {
                    markedOld.add(new MarkedVector2(oldr.get(i), true));
                    //newk.remove(j);
                    nomark = false;
                }
            }
            if (nomark) {
                markedOld.add(new MarkedVector2(oldr.get(i), false));
            }
            nomark = true;
        }

        System.out.println("ECKEN-------------------");
        MarkedVector2 last2 = markedOld.getLast();

        MarkedVector2 current2;
        MarkedVector2 next2;
        int k = 0;

        //ENTFERNEN DER DOPPELTEN ECKEN AUS newk
        for (MarkedVector2 v : markedOld) {



            current2 = markedOld.get(k);
            if(k == markedOld.size()-1) {
                k=-1;
            }
            next2 = markedOld.get(k + 1);
            if(current2.bool){
                System.out.println("Last: " + last2.v2.z + " x: " + last2.v2.x + " Boolean:" + last2.bool) ;
                System.out.println("Last: " + current2.v2.z + " x: " + current2.v2.x + " Boolean:" + current2.bool) ;
                System.out.println("Last: " + next2.v2.z + " x: " + next2.v2.x + " Boolean:" + next2.bool) ;
                if((next2.bool || last2.bool)){
                    System.out.println("TEST");
                    if(v.v2.equals(current2.v2)){

                        for(int o = 0; o < newk.size(); o++){
                            System.out.println(newk.get(o).x + "|" + newk.get(o).z);
                            System.out.println(current2.v2.x + "|" + current2.v2.z);
                            if(newk.get(o).equals(current2.v2)) {
                                System.out.println("Entferne:" + o);
                                newk.remove(o);
                            }
                        }
                    }
                }

            }
            last2 = current2;
            k++;

        }

        System.out.println("-----DDDDDDDDDDD-----");
        for (Vectore2 v: newk) {
            System.out.println("Ergebnis: A:" + v.z + " x:" + v.x);
        }




        System.out.println("-----BETRACHTUNG-----");
        // BETRACHTUNG

        int marker = -1;
        int index = 0;

        MarkedVector2 last = markedOld.getLast();
        MarkedVector2 current;
        MarkedVector2 next;
        int pairs = 0;

        for (MarkedVector2 v : markedOld) {

            if (pairs == 4) return Optional.empty();

            current = v;
            index++;
            if (index >= markedOld.size()) {
                index = 0;
            }

            next = markedOld.get(index);

            //System.out.println("------c:Debug------");
            //System.out.println("last:" + last.v2.z + "|" + last.v2.x + " curr:" + current.v2.z  + "|" + current.v2.x +  " next:" + next.v2.z + "|" + next.v2.x);

            System.out.println("TEST: " + current.v2.x + "," + current.v2.z);
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

        if (marker >= 0 && !newRegion.isEmpty()) {

            if(newRegion.size() == 2){
                if(newRegion.get(0).z == newRegion.get(1).z){
                    Collections.rotate(newRegion, 1);
                }
            }

            Collections.rotate(output, output.size() - marker);
            output.addAll(newRegion);
            Collections.rotate(output, marker + newRegion.size());
        }


        //DEBUG

        System.out.println("------a:Debug------");
        for (MarkedVector2 v : markedOld) {
            System.out.println("Ergebnis: z:" + v.v2.z + " x:" + v.v2.x + " bool:" + v.bool);
        }
        System.out.println("------b:Debug------");
        for (Vectore2 v : newk) {
            System.out.println("Ergebnis: z:" + v.z + " x:" + v.x);
        }
        System.out.println("------c:Debug------");
        System.out.println("Marker:" + marker);
        System.out.println("Paare:" + pairs + "/2 = " + pairs / 2);


        return Optional.of(output);


    }



    public static List<Vectore2> projezieren(List<Vectore2> current) {

        List<Vectore2> output = new ArrayList<>();

        int index = 1;
        Vectore2 next;

        for (Vectore2 list : current) {

            if (index == current.size()) {
                index = 0;
            }

            next = current.get(index);
            //System.out.println("------------------");
            //System.out.println(list.z + "|" + list.x);
            //System.out.println(next.z + "|" + next.x);


            if (list.x == next.x) {


                if (list.z > next.z) {
                    if (Math.abs(next.z - list.z) == 48) {
                        output.add(new Vectore2(list.z, list.x));
                        index++;
                        continue;
                    }
                    output.add(new Vectore2(list.z, list.x));
                    for (int i = 0; i < Math.abs(next.z - list.z) / 48 - 1; i++) {

                        output.add(new Vectore2(list.z - 48 * (i + 1), list.x));

                    }
                    index++;
                }
                if (list.z < next.z) {
                    if (Math.abs(next.z - list.z) == 48) {
                        output.add(new Vectore2(list.z, list.x));
                        index++;
                        continue;
                    }
                    output.add(new Vectore2(list.z, list.x));
                    for (int i = 0; i < Math.abs(list.z - next.z) / 48 - 1; i++) {
                        output.add(new Vectore2(list.z + 48 * (i + 1), list.x));
                    }

                    index++;
                }


            }

            if (list.z == next.z) {


                if (list.x > next.x) {
                    if (Math.abs(next.x - list.x) == 48) {
                        output.add(new Vectore2(list.z, list.x));
                        index++;
                        continue;
                    }
                    output.add(new Vectore2(list.z, list.x));
                    for (int i = 0; i < Math.abs(next.x - list.x) / 48 - 1; i++) {

                        output.add(new Vectore2(list.z, list.x - 48 * (i + 1)));

                    }
                    index++;
                }
                if (list.x < next.x) {
                    if (Math.abs(next.x - list.x) == 48) {
                        output.add(new Vectore2(list.z, list.x));
                        index++;
                        continue;
                    }
                    output.add(new Vectore2(list.z, list.x));
                    for (int i = 0; i < Math.abs(list.x - next.x) / 48 - 1; i++) {

                        output.add(new Vectore2(list.z, list.x + 48 * (i + 1)));

                    }
                    index++;
                }


            }


        }

        return output;

    }

    public static List<Vectore2> entprojezieren(List<Vectore2> current) {

        List<Vectore2> output = new ArrayList<>();

        Vectore2 last = current.getLast();
        Vectore2 next;
        int i = 0;

        for(Vectore2 list : current){

            if (i == current.size()-1) {
                i = 0;
            }

            next = current.get(i+1);

            if(list.x == last.x){
                if(list.x == next.x){
                    i++;
                    continue;
                }
            }
            if(list.z == last.z){
                if(list.z == next.z){
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


            /*
            if (firstValue) {
                output.add(new Vector2(list.z - 0.5f, list.x - 0.5f));

                index++;
                next = current.get(index);
                last = list;

                firstValue = false;
                continue;
            }
            */
            if (last.x == list.x) {
                //Bewegung auf der Z Achse (= bei x erste spalte und = bei z zweite spalte)
                if (list.z > last.z) {
                    //Bewegung in den + Bereich (+ z erste spalte)
                    if (list.x < next.x) {
                        //Bewegung in den + Bereich (+ x zweite spalte)
                        output.add(new Vectore2(list.z + 0.5f, list.x - 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                    if (list.x > next.x) {
                        //Bewegung in den - Bereich (- x zweite spalte)
                        output.add(new Vectore2(list.z - 0.5f, list.x - 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                }
                if (list.z < last.z) {
                    //Bewegung in den - Bereich (- z erste spalte)
                    if (list.x < next.x) {
                        //Bewegung in den + Bereich (+ x zweite spalte)
                        output.add(new Vectore2(list.z + 0.5f, list.x + 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                    if (list.x > next.x) {
                        //Bewegung in den - Bereich (- x zweite spalte)
                        output.add(new Vectore2(list.z - 0.5f, list.x + 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                }
            }
            if (last.z == list.z) {
                //Bewegung auf der X Achse (= bei z erste spalte und = bei x zweite spalte)
                if (list.x > last.x) {
                    //Bewegung in den + Bereich (+ x erste spalte)
                    if (list.z < next.z) {
                        //Bewegung in den + Bereich (+ z zweite spalte)
                        output.add(new Vectore2(list.z + 0.5f, list.x - 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                    if (list.z > next.z) {
                        //Bewegung in den - Bereich (- z zweite spalte)
                        output.add(new Vectore2(list.z + 0.5f, list.x + 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                }
                if (list.x < last.x) {
                    //Bewegung in den - Bereich (- x erste spalte)
                    if (list.z < next.z) {
                        //Bewegung in den + Bereich (+ z zweite spalte)
                        output.add(new Vectore2(list.z - 0.5f, list.x - 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);
                        continue;
                    }
                    if (list.z > next.z) {
                        //Bewegung in den - Bereich (- z zweite spalte)
                        output.add(new Vectore2(list.z - 0.5f, list.x + 0.5f));
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

    public static List<Vectore2> reverseaufplustern(List<Vectore2> current) {

        List<Vectore2> output = new ArrayList<>();

        boolean firstValue = true;
        int index = 1;
        Vectore2 last = current.getLast();
        Vectore2 next = current.get(1);


        for (Vectore2 list : current) {


            if (index == current.size() - 1) {
                index = 0;
            }


            /*
            if (firstValue) {
                output.add(new Vector2(list.z - 0.5f, list.x - 0.5f));

                index++;
                next = current.get(index);
                last = list;

                firstValue = false;
                continue;
            }
            */
            if (last.x == list.x) {
                //Bewegung auf der Z Achse (= bei x erste spalte und = bei z zweite spalte)
                if (list.z > last.z) {
                    //Bewegung in den + Bereich (+ z erste spalte)
                    if (list.x < next.x) {
                        //Bewegung in den + Bereich (+ x zweite spalte)
                        output.add(new Vectore2(list.z - 0.5f, list.x + 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                    if (list.x > next.x) {
                        //Bewegung in den - Bereich (- x zweite spalte)
                        output.add(new Vectore2(list.z + 0.5f, list.x + 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                }
                if (list.z < last.z) {
                    //Bewegung in den - Bereich (- z erste spalte)
                    if (list.x < next.x) {
                        //Bewegung in den + Bereich (+ x zweite spalte)
                        output.add(new Vectore2(list.z - 0.5f, list.x - 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                    if (list.x > next.x) {
                        //Bewegung in den - Bereich (- x zweite spalte)
                        output.add(new Vectore2(list.z + 0.5f, list.x - 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                }
            }
            if (last.z == list.z) {
                //Bewegung auf der X Achse (= bei z erste spalte und = bei x zweite spalte)
                if (list.x > last.x) {
                    //Bewegung in den + Bereich (+ x erste spalte)
                    if (list.z < next.z) {
                        //Bewegung in den + Bereich (+ z zweite spalte)
                        output.add(new Vectore2(list.z - 0.5f, list.x + 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                    if (list.z > next.z) {
                        //Bewegung in den - Bereich (- z zweite spalte)
                        output.add(new Vectore2(list.z - 0.5f, list.x - 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);

                        continue;
                    }
                }
                if (list.x < last.x) {
                    //Bewegung in den - Bereich (- x erste spalte)
                    if (list.z < next.z) {
                        //Bewegung in den + Bereich (+ z zweite spalte)
                        output.add(new Vectore2(list.z + 0.5f, list.x + 0.5f));
                        last = list;
                        index++;
                        next = current.get(index);
                        continue;
                    }
                    if (list.z > next.z) {
                        //Bewegung in den - Bereich (- z zweite spalte)
                        output.add(new Vectore2(list.z + 0.5f, list.x - 0.5f));
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
