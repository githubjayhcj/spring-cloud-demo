package com.example.webService;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * @ClassName: WebMagicTest
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/10/29 22:08
 * @Version: 1.0.0
 * @Description: 淘宝网首页商品列表数据爬取
 */
public class WebMagicTest implements PageProcessor {
    private Site site = Site.me()
            .setRetryTimes(3)
            .setSleepTime(100)
            .addHeader("Cookie","xlly_s=1; cookie2=1ee09ace6fdd5615fd7e2591696cb1b3; t=d56ba4c4e27861f4799abc9e83a2397c; _tb_token_=e95e3aeeeb576; _samesite_flag_=true; sgcookie=E100kXT2aJSKoDigZO2Cj7Aj2RXJtcc3k45C2vZ9Hkwn15WbRO3eEpvcGeFNrhLu%2FWsfLsHXwPP4N2MU6kGA9wYjAZHVtgYPO1aYCzLLxo42W3TPftx8P2K76vZ4PiqEUs%2BJ; _m_h5_tk=f7902f930a4ab932a02e9a1a5edceeb9_1698600836745; _m_h5_tk_enc=15647c2aa614c07bff2c8b7afc871479; mt=ci=0_0; tracknick=; cna=xX9HHWSn/DYCATpkS6LqRCKT; x5sec=7b22617365727665723b32223a22646665346562656331633362626337653932313766656132616365383363653743492f332b616b47454c364f6e655144476777794e6a63344f4451314d7a45774f7a45777338587a68667a2f2f2f2f2f41554144222c22733b32223a2262366638323265393631663566303165227d; isg=BCEhHhj54M5UOEw_vcV6Mp3TMOs7zpXAZ4yCvIP0gigR6ka8yh-AkBnrTB7sJS34; l=fBMierenPIdmT43GBO5Zourza77O3QRff1PzaNbMiIEGC6D5t-JiRL-QVRYrodxRR8XRGXLp4gZMp1vTQebY7mDmndLnZRpVB2MDBLb5JK1..; tfstk=dQrpA9ZtzOpL8xtL3BQGaTgSYSXGokeeXWyXqbcHP5FTn56Er_ySF0F_Z9l3F6P82fwz-2mIZ4h7i7Huxuq873e4UkXEq9oUL0o5isj02JyE4bd_5ZDPOr1_xsfcmiv9QyC1iyvWtZSiZ_LB8OKa7uMCryNEiXTDAvuKGRQ223TrCIczdxtWVfbo8OlumodZ7IcxEe6OBU8rRARvOmVG.");

    @Override
    public void process(Page page) {
//        page.addTargetRequests(page.getHtml().links().regex("(https://github\\.com/\\w+/\\w+)").all());
//        page.putField("author", page.getUrl().regex("https://github\\.com/(\\w+)/.*").toString());
//        page.putField("name", page.getHtml().xpath("//h1[@class='entry-title public']/strong/a/text()").toString());
//        if (page.getResultItems().get("name")==null){
//            //skip this page
//            page.setSkip(true);
//        }
//        page.putField("readme", page.getHtml().xpath("//div[@id='readme']/tidyText()"));

        System.out.println("process=------");
        System.out.println(page.getHtml());
        System.out.println("process 2222=------");
        System.out.println(page.getJson());

    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
//        Spider.create(new WebMagicTest()).addUrl("https://github.com/code4craft").thread(5).run();
//        Spider.create(new WebMagicTest()).addUrl("https://www.3839.com/fenlei/").thread(5).run();
        Spider.create(new WebMagicTest()).addUrl("https://h5api.m.taobao.com/h5/mtop.alimama.union.xt.en.api.entry/1.0/?jsv=2.5.1&appKey=12574478&t=1698593738760&sign=3883f0e77f9a3b039278622b876716d3&api=mtop.alimama.union.xt.en.api.entry&v=1.0&AntiCreep=true&timeout=20000&AntiFlood=true&type=jsonp&dataType=jsonp&callback=mtopjsonp2&data=%7B%22pNum%22%3A0%2C%22pSize%22%3A%2260%22%2C%22refpid%22%3A%22mm_26632258_3504122_32538762%22%2C%22variableMap%22%3A%22%7B%5C%22q%5C%22%3A%5C%22%E6%B7%98%E5%AE%9D%E8%B4%AD%E4%B9%B0%E7%BD%91%E5%BA%97%5C%22%2C%5C%22navigator%5C%22%3Afalse%2C%5C%22clk1%5C%22%3A%5C%226e8821d5ba063600f0fc02ca6b157a2a%5C%22%2C%5C%22recoveryId%5C%22%3A%5C%22201_33.8.101.132_60399805_1698593738506%5C%22%7D%22%2C%22qieId%22%3A%2236308%22%2C%22spm%22%3A%22a2e0b.20350158.31919782%22%2C%22app_pvid%22%3A%22201_33.8.101.132_60399805_1698593738506%22%2C%22ctm%22%3A%22spm-url%3A%3Bpage_url%3Ahttps%253A%252F%252Fuland.taobao.com%252Fsem%252Ftbsearch%253Frefpid%253Dmm_26632258_3504122_32538762%2526keyword%253D%2525E6%2525B7%252598%2525E5%2525AE%25259D%2525E8%2525B4%2525AD%2525E4%2525B9%2525B0%2525E7%2525BD%252591%2525E5%2525BA%252597%2526clk1%253D6e8821d5ba063600f0fc02ca6b157a2a%2526upsId%253D6e8821d5ba063600f0fc02ca6b157a2a%22%7D").thread(5).run();
    }
}
