package org.codehaus.mojo.chronos.data;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class TestCaseIdentifierHelper {

	private static TestCaseIdentifierHelper INSTANCE = new TestCaseIdentifierHelper();

	public static TestCaseIdentifierHelper instance() {
		return INSTANCE;
	}

	private PackageHolder base = new PackageHolder(null, null);
	private Map<String, PackageHolder> testCases = new HashMap<String, PackageHolder>();

	public String getIdentifier(String classname, String name) {
		PackageHolder packageHolder = testCases.get(classname);
		if (packageHolder == null) {
			packageHolder = base.addTestCase(new StringTokenizer(classname, "."));
			testCases.put(classname, packageHolder);
		}
        String result = packageHolder.getId() + "#" + name;
        return result;
	}

	private static class PackageHolder {

		private String name;
		private PackageHolder parent;
		private Map<String, PackageHolder> children = new HashMap<String, PackageHolder>();

		/* pp */PackageHolder(String name, PackageHolder parent) {
			this.name = name;
			this.parent = parent;
		}

		public String getId() {
			if (name == null) {
				return null;
			}

			String parentId = parent.getId();
            if (parentId == null) {
				return name;
			} else {
                return parentId + "." + name;
            }
		}

		public PackageHolder addTestCase(StringTokenizer st) {
			if (!st.hasMoreTokens()) {
				return this;
			}

			String _package = st.nextToken();
			if (children.containsKey(_package)) {
				return children.get(_package).addTestCase(st);
			} else {
				PackageHolder packageHolder = new PackageHolder(_package, this);
				children.put(_package, packageHolder);
				return packageHolder.addTestCase(st);
			}
		}
	}
}