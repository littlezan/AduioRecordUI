package com.example.administrator.aduiorecordui;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ClassName: TestList
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-06-25  19:20
 */
public class TestList {

    @Test
    public void test01() {
        ArrayList<Long> longArrayList = new ArrayList<>();
        longArrayList.add(1L);
        longArrayList.add(2L);
        longArrayList.add(3L);
        longArrayList.add(4L);
        if (longArrayList.contains(1L)) {
            System.out.println("yyyyyy");
        }
        longArrayList.subList(longArrayList.indexOf(3L), longArrayList.size()).clear();
        System.out.println(Arrays.toString(longArrayList.toArray()));
    }

    @Test
    public void test02() {
        ArrayList<String> stringArrayList = new ArrayList<>();
        String  index = "" ;
        for (int i = 0; i < 10; i++) {
            if (i == 5) {
                index = "" + i;
            }
            stringArrayList.add("" + i);
        }

        List<String> result = stringArrayList.subList(0, stringArrayList.indexOf(index));

        System.out.println("stringArrayList = "+ Arrays.toString(stringArrayList.toArray()));

        System.out.println("result = "+ Arrays.toString(result.toArray()));


    }
}
