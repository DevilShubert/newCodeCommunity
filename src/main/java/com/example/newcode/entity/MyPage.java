package com.example.newcode.entity;

/**
 * 封装分页相关的信息.
 */
public class MyPage {
    // 当前页码
    private int current = 1;
    // 显示上限
    private int limit = 10;
    // 查询路径(用于复用分页链接)
    private String path;
    // 设置查询总页数
    private int total;
    // 总记录数
    private int rows;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 得到总页数
     * @return
     */
    public int getTotal() {
        return total;
    }


    /**
     * 设置总页数
     * @param total
     */
    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * 获取起始页码
     *
     * @return
     */
    public int getFrom() {
        int from = current - 3;
        return from < 1 ? 1 : from;
    }

    /**
     * 获取结束页码
     *
     * @return
     */
    public int getTo() {
        int to = current + 3;
        int total = getTotal();
        return to > total ? total : to;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }
}
