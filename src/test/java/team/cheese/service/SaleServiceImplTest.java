package team.cheese.service;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import team.cheese.dao.AdministrativeDao;
import team.cheese.dao.SaleCategoryDao;
import team.cheese.dao.SaleDao;
import team.cheese.domain.SaleDto;
import team.cheese.service.sale.SaleService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/spring/**/root-context.xml"})
public class SaleServiceImplTest extends TestCase {
    @Autowired
    SaleDao saleDao;
    @Autowired
    SaleCategoryDao saleCategoryDao;
    @Autowired
    AdministrativeDao administrativeDao;

    @Autowired
    SaleService saleService;

    @Test
    public void testGetCount() {
    }

    @Test
    public void testRemove() {
    }

    @Test
    public void testWrite() {
    }

    @Test
    public void testGetList() throws Exception {
        String addr_cd = null;
        String sal_i_cd = null;
        Map map = new HashMap();
        List<SaleDto> list = saleService.getList(map);
        System.out.println(list.size());
        assertTrue(list.size() != 0);
    }

    @Test
    public void testRead() throws Exception {
        Long no = Long.valueOf(50);
        Map map = saleService.read(no);
        SaleDto saleDto = (SaleDto) map.get("saleDto");
        System.out.println(saleDto.getNo());
        assertTrue(saleDto.getNo().equals(no));
    }

    @Test
    public void testModify() {
    }
}