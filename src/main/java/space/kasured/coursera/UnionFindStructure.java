package space.kasured.coursera;

interface UnionFindStructure<Group, Element> {
    Group findGroupFor(Element element);
    void union(Element element, Element otherElement);
}
