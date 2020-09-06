import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
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
	//datafactory
	OWLDataFactory df = manager.getOWLDataFactory();
	//rename method for concept names of type existential restrictions
	//input: ObjectSomeValuesFrom, for existential restriction generate new names
	//The new names naming is done according to the following:
	//if rg restriction then give name general name
	//for restrictions under rg, give specific names
	int num_rg = 0;
	
//	public void rename_obsv(OWLObjectSomeValuesFrom obsv) {
//		
//		OWLClassExpression obsv_filler = obsv.getFiller();
//		
//		if(obsv_filler instanceof OWLObjectIntersectionOf) {
//			//give general name to obsv_filler
//			
//			String general_obsv_name = obsv_filler.toString() + "_i_" + num;
//			OWLObjectSomeValuesFrom obsv_in = (OWLObjectSomeValuesFrom) obsv_filler;
//			//we could have more than one obsv_in inside obsv, check if obs
//			
//			
//			Set<OWLClassExpression> obsv_filler_conjuncts = obsv_filler.asConjunctSet();
//			for(OWLClassExpression obsv_filler_conjunct: obsv_filler_conjuncts) {
//				
//			}
//			
//			
//			
//		}
//		//rg some r some B
//		if(obsv_filler instanceof OWLObjectSomeValuesFrom) {
//			//give general name to obsv_filler
//			String general_obsv_name = obsv_filler.toString() + "_i_" + num;
//			OWLObjectSomeValuesFrom obsv_in = (OWLObjectSomeValuesFrom) obsv_filler;
//			//we could have more than one obsv_in inside obsv, check if obs
//			
//		}
//	}
	
public Map<OWLClass, Set<OWLClass>> from_obsv_to_owlclass(OWLObjectSomeValuesFrom obsv) {
		num_rg++;
		int num_pv = 0;
		Set<OWLClass> obsv_new_owlclasses = new HashSet<>();
		Map<OWLClass, Set<OWLClass>> g_s_classes_map = new HashMap<>();
		//String general_obsv_name = obsv.toString() + "_g_";
		String general_obsv_name = "PVRG_" + num_rg;
		OWLClass cl_general = df.getOWLClass(IRI.create(general_obsv_name));
		OWLClassExpression obsv_filler = obsv.getFiller();
		//rg some (r some B and ...)
		if(obsv_filler instanceof OWLObjectIntersectionOf) {
			//this is nested type obsv
			//give general name to obsv_filler
			//we could have more than one obsv_in inside obsv, check if obs
			//obsv_new_owlclasses.add(cl_general);
			
			Set<OWLClassExpression> obsv_filler_conjuncts = obsv_filler.asConjunctSet();
			for(OWLClassExpression obsv_filler_conjunct: obsv_filler_conjuncts) {
				if(obsv_filler_conjunct instanceof OWLObjectSomeValuesFrom) {
					num_pv++;
					//String specific_obsv_name = obsv_filler_conjunct.toString();
					String specific_obsv_name = "PV_" + num_rg + "_" + num_pv;
					OWLClass cl_specific = df.getOWLClass(IRI.create(specific_obsv_name));
					obsv_new_owlclasses.add(cl_specific);
					//call from_owlclass_to_obsv to test it from here
					//from_owlclass_to_obsv(cl_general);
				}
			}
			
			g_s_classes_map.put(cl_general, obsv_new_owlclasses);
		}
		//rg some r some B
		if(obsv_filler instanceof OWLObjectSomeValuesFrom) {
			//this is nested type obsv
			//give general name to obsv_filler
			
			//OWLObjectSomeValuesFrom obsv_in = (OWLObjectSomeValuesFrom) obsv_filler;
			//String specific_obsv_name = obsv_filler.toString() + "_s_";
			num_pv++;
			String specific_obsv_name = "PV_" + num_rg + "_" + num_pv;
			OWLClass cl_specific = df.getOWLClass(IRI.create(specific_obsv_name));
			//to differentiate between the cl_general and cl_specific, I will assign special character g to cl_general and s to cl_spcefic 
			//obsv_new_owlclasses.add(cl_general);
			obsv_new_owlclasses.add(cl_specific);
			
			g_s_classes_map.put(cl_general, obsv_new_owlclasses);
		}
		//what about if the restriction is simple?
		//in order to get names back (The object some Values from structure, then in this case how to make sure I ) 
		
		return g_s_classes_map;
	}


public Map<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>> from_obsv_to_owlclass_2(OWLObjectSomeValuesFrom obsv) {
	num_rg++;
	int num_pv = 0;
	//Set<OWLClass> obsv_new_owlclasses = new HashSet<>();
	Map<OWLObjectSomeValuesFrom, OWLClass> pv_s_classes = new HashMap<>();
	Map<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>> g_s_classes_map = new HashMap<>();
	//String general_obsv_name = obsv.toString() + "_g_";
	
	PrefixManager pm = new DefaultPrefixManager();
	StringComparator sc = pm.getPrefixComparator();
	DefaultPrefixManager dm = new DefaultPrefixManager(pm, sc, "http://snomed.info/id/");
	
	
	String general_obsv_name = "PVRG_" + num_rg;
	
	OWLClass cl_general = df.getOWLClass(general_obsv_name, dm);
	System.out.println("the cl_general: " + cl_general);
	OWLClassExpression obsv_filler = obsv.getFiller();
	//rg some (r some B and ...)
	if(obsv_filler instanceof OWLObjectIntersectionOf) {
		//this is nested type obsv
		//give general name to obsv_filler
		//we could have more than one obsv_in inside obsv, check if obs
		//obsv_new_owlclasses.add(cl_general);
		
		Set<OWLClassExpression> obsv_filler_conjuncts = obsv_filler.asConjunctSet();
		for(OWLClassExpression obsv_filler_conjunct: obsv_filler_conjuncts) {
			if(obsv_filler_conjunct instanceof OWLObjectSomeValuesFrom) {
				num_pv++;
				OWLObjectSomeValuesFrom obsv_pv = (OWLObjectSomeValuesFrom) obsv_filler_conjunct;
				//String specific_obsv_name = obsv_filler_conjunct.toString();
				String specific_obsv_name = "PV_" + num_rg + "_" + num_pv;
				
				OWLClass cl_specific = df.getOWLClass(specific_obsv_name, dm);
				//obsv_new_owlclasses.add(cl_specific);
				pv_s_classes.put(obsv_pv, cl_specific);
				//call from_owlclass_to_obsv to test it from here
				//from_owlclass_to_obsv(cl_general);
			}
		}
		
		g_s_classes_map.put(cl_general, pv_s_classes);
	}
	//rg some r some B
	if(obsv_filler instanceof OWLObjectSomeValuesFrom) {
		//this is nested type obsv
		//give general name to obsv_filler
		OWLObjectSomeValuesFrom obsv_pv = (OWLObjectSomeValuesFrom) obsv_filler;
		//OWLObjectSomeValuesFrom obsv_in = (OWLObjectSomeValuesFrom) obsv_filler;
		//String specific_obsv_name = obsv_filler.toString() + "_s_";
		num_pv++;
		String specific_obsv_name = "PV_" + num_rg + "_" + num_pv;
		OWLClass cl_specific = df.getOWLClass(specific_obsv_name, dm);
		//to differentiate between the cl_general and cl_specific, I will assign special character g to cl_general and s to cl_spcefic 
		//obsv_new_owlclasses.add(cl_general);
		//obsv_new_owlclasses.add(cl_specific);
		pv_s_classes.put(obsv_pv, cl_specific);
		g_s_classes_map.put(cl_general, pv_s_classes);
	}
	//what about if the restriction is simple?
	//in order to get names back (The object some Values from structure, then in this case how to make sure I ) 
	
	return g_s_classes_map;
}
	//to convert from owlclass to obsv, I just need the name with _g_ to create the assciated obsv? not sure, neeed test!
/*public Set<OWLClass> from_owlclass_to_obsv(Set<OWLClass> owlclasses) {
	//change the set return type
	Set<OWLClass> owlclasses_to_obsvs = new HashSet<>();
	//use both geerals and specific names
	//use a map to map the general names with their specifics
	String owlclass_name = owlclass.toString();
	if(owlclass_name.contains("_g_")) {
		System.out.println("current owlclass_name with _g_: " + owlclass_name);
		if(owlclass_name.contains("http://snomed.info/id/609096000")) {
			//how to get the specific parts?
			//if the name contains ObjectIntersectionOf then .. 
			//th
		}
		//OWLObjectSomeValuesFrom obsv = df.getOWLObjectSomeValuesFrom(arg0, arg1)
	}
	

	//what about if the restriction is simple?
	//in order to get names back (The object some Values from structure, then in this case how to make sure I ) 
	return owlclasses_to_obsvs;
}*/
	
	
}


