package com.example.newcode.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SensitiveFilter {
    // replace String
    private static final String REPLACEMENT = "***";

    private Trie rootNode = new Trie();

    @PostConstruct
    public void init(){
        try(
                // get txt by JavaIO
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // add into prefix tree
                this.addKeyWord(keyword);
            }
        } catch (IOException e) {
            log.error("加载敏感词文件失败: " + e.getMessage());
        }
    }

    /**
     * Add sensitive word to build prefix tree
     * @param keyWord sensitive word
     */
    public void addKeyWord(String keyWord){
        Trie tempNode = rootNode;
        for (int i = 0; i < keyWord.length(); i++){
            Character s = keyWord.charAt(i);
            Trie subNodes = tempNode.getSubNodes(s);

            // keyWord's first char dose not exist
            if (subNodes == null) {
                subNodes = new Trie();
                tempNode.setSubNodes(s, subNodes);
            }

            // keyWord's first char exists
            tempNode = subNodes;

            if (i == keyWord.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }

    }


    /**
     * Do replace the sensitive word
     * @param text
     * @return filter result
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        // 指针1
        Trie tempNode = rootNode;
        // 指针2（左）
        int begin = 0;
        // 指针3（右）
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();

        while(begin < text.length()){
            if(position < text.length()) {
                Character c = text.charAt(position);

                // 跳过符号
                if (isSymbol(c)) {
                    if (tempNode == rootNode) {
                        begin++;
                        sb.append(c);
                    }
                    position++;
                    continue;
                }

                // 检查下级节点
                tempNode = tempNode.getSubNodes(c);
                if (tempNode == null) {
                    // 以begin开头的字符串不是敏感词
                    sb.append(text.charAt(begin));
                    // 进入下一个位置
                    position = ++begin;
                    // 重新指向根节点
                    tempNode = rootNode;
                }
                // 发现敏感词
                else if (tempNode.isKeywordEnd()) {
                    sb.append(REPLACEMENT);
                    begin = ++position;
                    tempNode = rootNode;
                }
                // 检查下一个字符
                else {
                    position++;
                }
            }
            // 这里的else目的是为了处理position遍历越界仍未匹配到敏感词，进入下面else有3总情况
            // 1、没有进入敏感词匹配正常结束；2、刚好匹配到敏感词结束；3、敏感词匹配到一般position；
            else{
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            }
        }
        return sb.toString();
    }


    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    private class Trie{
        // sensitive word end flag
        private boolean isKeywordEnd = false;

        // subTrie
        private Map<Character, Trie> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }
        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // add subNode
        public void setSubNodes(Character s, Trie subNode) {
            this.subNodes.put(s, subNode);
        }

        public Trie getSubNodes(Character s) {
            return subNodes.get(s);
        }
    }
}
