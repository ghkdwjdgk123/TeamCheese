package team.cheese.domain;

import java.sql.Timestamp;
import java.util.Objects;

public class FaqDto {

    private long no; // 변경된 부분
    private long que_i_cd; // 변경된 부분
    private String title;
    private String contents;
    private String ad_id;
    private Timestamp r_date;
    private Timestamp m_date;
    private int view_cnt = 0;
    private char state = 'Y';
    private Timestamp first_date;
    private String first_id;
    private Timestamp last_date;
    private String last_id;

    public FaqDto(){}

    public FaqDto(long no, long que_i_cd, String title, String contents, String ad_id, int view_cnt, char state, String first_id, String last_id) { // 변경된 부분
        this.no = no;
        this.que_i_cd = que_i_cd;
        this.title = title;
        this.contents = contents;
        this.ad_id = ad_id;
        this.view_cnt = view_cnt;
        this.state = state;
        this.first_id = first_id;
        this.last_id = last_id;
    }

    public long getNo() { // 변경된 부분
        return no;
    }

    public void setNo(long no) { // 변경된 부분
        this.no = no;
    }

    public long getQue_i_cd() { // 변경된 부분
        return que_i_cd;
    }

    public void setQue_i_cd(long que_i_cd) { // 변경된 부분
        this.que_i_cd = que_i_cd;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getAd_id() {
        return ad_id;
    }

    public void setAd_id(String ad_id) {
        this.ad_id = ad_id;
    }

    public Timestamp getR_date() {
        return r_date;
    }

    public void setR_date(Timestamp r_date) {
        this.r_date = r_date;
    }

    public Timestamp getM_date() {
        return m_date;
    }

    public void setM_date(Timestamp m_date) {
        this.m_date = m_date;
    }

    public int getView_cnt() {
        return view_cnt;
    }

    public void setView_cnt(int view_cnt) {
        this.view_cnt = view_cnt;
    }

    public char getState() {
        return state;
    }

    public void setState(char state) {
        this.state = state;
    }

    public Timestamp getFirst_date() {
        return first_date;
    }

    public void setFirst_date(Timestamp first_date) {
        this.first_date = first_date;
    }

    public String getFirst_id() {
        return first_id;
    }

    public void setFirst_id(String first_id) {
        this.first_id = first_id;
    }

    public Timestamp getLast_date() {
        return last_date;
    }

    public void setLast_date(Timestamp last_date) {
        this.last_date = last_date;
    }

    public String getLast_id() {
        return last_id;
    }

    public void setLast_id(String last_id) {
        this.last_id = last_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FaqDto faqDto = (FaqDto) o;
        return no == faqDto.no && que_i_cd == faqDto.que_i_cd && state == faqDto.state && view_cnt == faqDto.view_cnt && Objects.equals(title, faqDto.title) && Objects.equals(contents, faqDto.contents) && Objects.equals(ad_id, faqDto.ad_id) && Objects.equals(first_id, faqDto.first_id) && Objects.equals(last_id, faqDto.last_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(no, que_i_cd, title, contents, ad_id, view_cnt, state, first_id, last_id);
    }

    @Override
    public String toString() {
        return "FaqDto{" +
                "no=" + no +
                ", que_i_cd=" + que_i_cd +
                ", title='" + title + '\'' +
                ", contents='" + contents + '\'' +
                ", ad_id='" + ad_id + '\'' +
                ", r_date=" + r_date +
                ", m_date=" + m_date +
                ", view_cnt=" + view_cnt +
                ", state=" + state +
                ", first_date=" + first_date +
                ", first_id='" + first_id + '\'' +
                ", last_date=" + last_date +
                ", last_id='" + last_id + '\'' +
                '}';
    }
}
