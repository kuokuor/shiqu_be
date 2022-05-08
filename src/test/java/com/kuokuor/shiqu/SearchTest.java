package com.kuokuor.shiqu;

import com.kuokuor.shiqu.dao.NoteDao;
import com.kuokuor.shiqu.entity.Note;
import com.kuokuor.shiqu.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 搜索测试
 *
 * @Author: GreatBiscuit
 * @Date: 2022/5/8 20:03
 */
@SpringBootTest
public class SearchTest {

    @Autowired
    private NoteDao noteDao;

    @Autowired
    private SearchService searchService;

    @Test
    public void saveNote() {
        Note note = noteDao.queryById(1);
        searchService.saveNote(note);
        note = noteDao.queryById(2);
        searchService.saveNote(note);
    }


}
