package com.m3958.apps.lucenesolr;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Test;

public class AnalyzerTest {

  /**
   * 1、Parsing，这个在lucene范围之外，比如将word，pdf，excel，html等变成纯文本
   */

  /**
   * 2、Tokenization，也包括pre-tokenization或者post-tokenization。pre包括strip
   * html等，post包括，stemming（词干化），stop words filtering（剔除干扰词，and an etc），Text
   * Normalization（去除口音，方言等），Synonym（同义词扩展）
   */

  /**
   * Analysis 涉及：
   * Analyzer，负责新建TokenStream。CharFilter，对原始文本进行替换、插入、删除等操作，同时记录偏移，让highlight等工具可以正确设别位置。Tokenizer和TokenFilter.
   */
  /**
   * 
   * TokenStream枚举token序列，是一个抽象类，有Tokenizer和TokenFilter两个子类，Tokenizer的输入时Reader，
   * 而TokenFilter的输入时Tokenizer。
   * 
   * TokenStream的工作流程： 1、实例化TokenStream，它会get/set attributes to/from
   * AttributeSource。TokenStream继承自AttributeSource 2、The consumer calls reset(). 3、 The consumer
   * retrieves attributes from the stream and stores local references to all attributes it wants to
   * access.4、The consumer calls incrementToken() until it returns false consuming the attributes
   * after each call.5、The consumer calls end() so that any end-of-stream operations can be
   * performed.6、The consumer calls close() to release any resource when finished using the
   * TokenStream.
   * 
   */

  /**
   * 为了确保consumer和filter知道那些attribute可用，attributes必须在TokenStream实例化的时候指定。 Tokenizer也是抽象类！！
   */
  @Test
  public void t1() throws IOException {
    Version matchVersion = Version.LUCENE_48; // Substitute desired Lucene version for XY
    Analyzer analyzer = new StandardAnalyzer(matchVersion); // or any other analyzer
    TokenStream ts = analyzer.tokenStream("myfield", new StringReader("some text goes here"));
    OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);

    try {
      ts.reset(); // Resets this stream to the beginning. (Required)
      while (ts.incrementToken()) {
        // Use AttributeSource.reflectAsString(boolean)
        // for token stream debugging.
        System.out.println("token: " + ts.reflectAsString(true));

        // Tokenizer 继承自AttributeSource。
        OffsetAttribute as = ts.getAttribute(OffsetAttribute.class);

        // 说明对于单个TokenStream来说，attribute只实例化一个。
        Assert.assertSame(as, offsetAtt);

        System.out.println("token start offset: " + offsetAtt.startOffset());
        System.out.println("  token end offset: " + offsetAtt.endOffset());
      }
      ts.end(); // Perform end-of-stream operations, e.g. set the final offset.
    } finally {
      ts.close(); // Release resources associated with this stream.
      analyzer.close();
    }
  }
  
  /**
   * Analyzer已经将Tokenizer，TokenFilter等必要的组件组合其来，直接使用analyzer即可。
   * @throws IOException 
   */
  
  @Test
  public void t2() throws IOException{
    Version matchVersion = Version.LUCENE_48; // Substitute desired Lucene version for XY
    Analyzer analyzer = new SmartChineseAnalyzer(matchVersion); // or any other analyzer
    
    TokenStream ts = analyzer.tokenStream("myfield", new StringReader("我是中国人"));

    CharTermAttribute cta = ts.addAttribute(CharTermAttribute.class);
    List<String> charTerms = new ArrayList<>();
    try {
      ts.reset(); // Resets this stream to the beginning. (Required)
      while (ts.incrementToken()) {
        // Use AttributeSource.reflectAsString(boolean)
        // for token stream debugging.
        // Tokenizer 继承自AttributeSource。
        CharTermAttribute as = ts.getAttribute(CharTermAttribute.class);

        // 说明对于单个TokenStream来说，attribute只实例化一个。
        Assert.assertSame(as, cta);
        charTerms.add(as.toString());
      }
      ts.end(); // Perform end-of-stream operations, e.g. set the final offset.
    } finally {
      ts.close(); // Release resources associated with this stream.
      analyzer.close();
    }
    
    Assert.assertEquals("我", charTerms.get(0));
    Assert.assertEquals("是", charTerms.get(1));
    Assert.assertEquals("中国", charTerms.get(2));
    Assert.assertEquals("人", charTerms.get(3));
  }
  
  @Test
  public void t3() throws IOException{
    Version matchVersion = Version.LUCENE_48; // Substitute desired Lucene version for XY
    Analyzer analyzer = new SmartChineseAnalyzer(matchVersion); // or any other analyzer
    
    TokenStream tsc = analyzer.tokenStream("myfield", new StringReader("我是中国人"));
    
    RemoveDuplicatesTokenFilter ts = new RemoveDuplicatesTokenFilter(tsc);

    CharTermAttribute cta = ts.addAttribute(CharTermAttribute.class);
    List<String> charTerms = new ArrayList<>();
    try {
      ts.reset(); // Resets this stream to the beginning. (Required)
      while (ts.incrementToken()) {
        // Use AttributeSource.reflectAsString(boolean)
        // for token stream debugging.
        // Tokenizer 继承自AttributeSource。
        CharTermAttribute as = ts.getAttribute(CharTermAttribute.class);

        // 说明对于单个TokenStream来说，attribute只实例化一个。
        Assert.assertSame(as, cta);
        charTerms.add(as.toString());
      }
      ts.end(); // Perform end-of-stream operations, e.g. set the final offset.
    } finally {
      ts.close(); // Release resources associated with this stream.
      analyzer.close();
    }
    
    Assert.assertEquals("我", charTerms.get(0));
    Assert.assertEquals("是", charTerms.get(1));
    Assert.assertEquals("中国", charTerms.get(2));
    Assert.assertEquals("人", charTerms.get(3));
  }
}
