package com.example.administrator.aduiorecordui;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

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
}
