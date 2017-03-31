/*
 * The MIT License
 *
 * Original work sponsored and donated by National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
 * Further enhancement before move to Codehaus sponsored and donated by Lakeside A/S (http://www.lakeside.dk)
 *
 * Copyright (c) to all contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * $HeadURL: https://svn.codehaus.org/mojo/trunk/sandbox/chronos-maven-plugin/chronos/src/main/java/org/codehaus/mojo/chronos/jmeter/JMeterMojo.java $
 * $Id: JMeterMojo.java 14221 2011-06-24 10:16:28Z soelvpil $
 */
package org.codehaus.mojo.chronos.data;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "testcase", uniqueConstraints = { @UniqueConstraint(columnNames = { "classname", "name" }) })
@NamedQueries({ @NamedQuery(name = "testcase.byClassAndName", query = "select tc from TestCase as tc where tc.classname = :classname and tc.name = :name"), @NamedQuery(name = "testcase.byClass", query = "select tc from TestCase as tc where tc.classname = :classname"),
		@NamedQuery(name = "testcase.all", query = "select tc from TestCase as tc") })
public class TestCase {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	protected Long id;

	@Column(name = "basetime")
	private int basetime;

	@Column(length = 1024)
	private String classname;

	@Column(length = 512)
	private String name;

	@OneToMany(fetch = FetchType.LAZY)
	@IndexColumn(name = "index", base = 1)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	@LazyCollection(LazyCollectionOption.EXTRA)
	private List<TestCaseResult> results = new ArrayList<TestCaseResult>();

	public TestCase() {
		// Do nothing
	}

	public TestCase( String classname, String name ) {
		this.classname = classname;
		this.name = name;
	}

	public String getTestIdentifier() {
		return TestCaseIdentifierHelper.instance().getIdentifier(classname, name);
	}

    public void addExecution(boolean succes, long startTime, int executionTime) {
        TestCaseResult testCaseResult =
            new TestCaseResult( succes, startTime, executionTime );
        results.add( testCaseResult );
    }

	public List<TestCaseResult> getResults() {
		return results;
	}

	public String getClassname() {
		return classname;
	}

    @Override
	public boolean equals(Object obj) {
		if (obj instanceof TestCase) {
			TestCase other = (TestCase) obj;
			return this.classname.equals(other.classname) && this.name.equals(other.name);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return classname.hashCode() + name.hashCode();
	}

	@Override
	public String toString() {
		return "TestCase[classname=" + classname + ", name=" + name + ", basetime=" + basetime + "]";
	}
}