package com.kuokuor.shiqu;

import com.kuokuor.shiqu.dao.TagDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Tag测试
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/5 23:12
 */
@SpringBootTest
public class TagsTest {

    @Autowired
    private TagDao tagDao;

    @Test
    public void insertNoteTags() {
        int[] tags = {1, 2, 3, 4};
        tagDao.insertNoteTags(2, tags);
    }

    @Test
    public void deleteNoteTags() {
        tagDao.deleteNoteTags(1);
    }

    @Test
    public void selectNoteTags() {
        int[] tags = tagDao.selectNoteTags(1);
        for (int tag : tags) {
            System.out.println(tag);
        }
    }

    @Test
    public void selectNoteByTag() {
        int[] notes = tagDao.selectNoteByTag(2);
        for (int noteId : notes) {
            System.out.println(noteId);
        }
    }

}
