import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.StringComparator;

/**
ghadahalghamdi
*/

public class Rename {

	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	
	OWLDataFactory df = manager.getOWLDataFactory();

	int num_rg = 0;

	

 
 
public Map<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>> from_obsv_to_owlclass(OWLObjectSomeValuesFrom obsv) {
	num_rg++;
	int num_pv = 0;
	Map<OWLObjectSomeValuesFrom, OWLClass> pv_s_classes = new HashMap<>();
	Map<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>> g_s_classes_map = new HashMap<>();
	
	PrefixManager pm = new DefaultPrefixManager();
	StringComparator sc = pm.getPrefixComparator();
	DefaultPrefixManager dm = new DefaultPrefixManager(pm, sc, "http://snomed.info/id/");
	
	
	
	OWLClassExpression obsv_filler = obsv.getFiller();
	//rg some (r some B and ...)
	if(obsv_filler instanceof OWLObjectIntersectionOf) {
		
		String general_obsv_name = "PVRG_" + num_rg;
		
		OWLClass cl_general = df.getOWLClass(general_obsv_name, dm);
		//System.out.println("the cl_general: " + cl_general);
		//System.out.println("inside rename class: the filler is: object intersection of");
		Set<OWLClassExpression> obsv_filler_conjuncts = obsv_filler.asConjunctSet();
		for(OWLClassExpression obsv_filler_conjunct: obsv_filler_conjuncts) {
			if(obsv_filler_conjunct instanceof OWLObjectSomeValuesFrom) {
				num_pv++;
				OWLObjectSomeValuesFrom obsv_pv = (OWLObjectSomeValuesFrom) obsv_filler_conjunct;
				String specific_obsv_name = "PV_" + num_rg + "_" + num_pv;
				
				OWLClass cl_specific = df.getOWLClass(specific_obsv_name, dm);
				pv_s_classes.put(obsv_pv, cl_specific);
				
			}
		}
		
		g_s_classes_map.put(cl_general, pv_s_classes);
	}
	//rg some r some B
	if(obsv_filler instanceof OWLObjectSomeValuesFrom) {
		String general_obsv_name = "PVRG_" + num_rg;
		
		OWLClass cl_general = df.getOWLClass(general_obsv_name, dm);
		System.out.println("the cl_general: " + cl_general);
		
		System.out.println("inside rename class: the filler is: obsv");
		OWLObjectSomeValuesFrom obsv_pv = (OWLObjectSomeValuesFrom) obsv_filler;

		num_pv++;
		String specific_obsv_name = "PV_" + num_rg + "_" + num_pv;
		OWLClass cl_specific = df.getOWLClass(specific_obsv_name, dm);
	
		pv_s_classes.put(obsv_pv, cl_specific);
		g_s_classes_map.put(cl_general, pv_s_classes);
		
	}
	if(obsv_filler instanceof OWLClass) {
		//System.out.println("inside rename class: the filler is: owlclass (simple existential restriction)");
		String general_obsv_name = "PVS_" + num_rg;
		OWLClass cl_general = df.getOWLClass(general_obsv_name, dm); 
		pv_s_classes.put(obsv, cl_general);
		g_s_classes_map.put(cl_general, pv_s_classes);
	}

	
	return g_s_classes_map;
}

/*	public Map<OWLClass, Set<OWLClass>> from_obsv_to_owlclass(OWLObjectSomeValuesFrom obsv) {
num_rg++;
int num_pv = 0;
Set<OWLClass> obsv_new_owlclasses = new HashSet<>();
Map<OWLClass, Set<OWLClass>> g_s_classes_map = new HashMap<>();
String general_obsv_name = "PVRG_" + num_rg;
OWLClass cl_general = df.getOWLClass(IRI.create(general_obsv_name));
OWLClassExpression obsv_filler = obsv.getFiller();
//rg some (r some B and ...)
if(obsv_filler instanceof OWLObjectIntersectionOf) {
	
	
	Set<OWLClassExpression> obsv_filler_conjuncts = obsv_filler.asConjunctSet();
	for(OWLClassExpression obsv_filler_conjunct: obsv_filler_conjuncts) {
		if(obsv_filler_conjunct instanceof OWLObjectSomeValuesFrom) {
			num_pv++;
			String specific_obsv_name = "PV_" + num_rg + "_" + num_pv;
			OWLClass cl_specific = df.getOWLClass(IRI.create(specific_obsv_name));
			obsv_new_owlclasses.add(cl_specific);
		}
	}
	
	g_s_classes_map.put(cl_general, obsv_new_owlclasses);
}
//rg some r some B
if(obsv_filler instanceof OWLObjectSomeValuesFrom) {

	num_pv++;
	String specific_obsv_name = "PV_" + num_rg + "_" + num_pv;
	OWLClass cl_specific = df.getOWLClass(IRI.create(specific_obsv_name));

	obsv_new_owlclasses.add(cl_specific);
	
	g_s_classes_map.put(cl_general, obsv_new_owlclasses);
}

return g_s_classes_map;
}*/	
	
}


