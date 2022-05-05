package com.kuokuor.shiqu;

import com.kuokuor.shiqu.dao.ImageDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 笔记中图片测试类
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/5 22:51
 */
@SpringBootTest
public class NoteImagesTest {

    @Autowired
    private ImageDao imageDao;

    @Test
    public void insertNoteImages() {
        String[] images = {"http://images.hyxk.xyz/HYXK.jpg", "http://images.hyxk.xyz/HYXK.jpg", "http://images.hyxk.xyz/HYXK.jpg"};
        imageDao.insertNoteImages(2, images);
    }

    @Test
    public void deleteNoteImages() {
        imageDao.deleteNoteImages(1);
    }

    @Test
    public void selectNoteImages() {
        String[] images = imageDao.selectNoteImages(1);
        for (String i : images) {
            System.out.println(i);
        }
    }

}
