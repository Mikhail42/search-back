package org.ionkin.search.xmlparser.model;

import javax.xml.bind.annotation.*;

@XmlRootElement(name="page")
@XmlAccessorType(XmlAccessType.FIELD)
public class WikiPage {
    @XmlElement(name="id")
    private int id;
    @XmlElement(name="title")
    private String title;
    @XmlElement(name = "redirect")
    private Redirect redirect;
    @XmlElement(name="revision")
    private Revision revision;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Revision getRevision() {
        return revision;
    }

    public void setRevision(Revision revision) {
        this.revision = revision;
    }

    public Redirect getRedirect() {
        return redirect;
    }

    public void setRedirect(Redirect redirect) {
        this.redirect = redirect;
    }
}
