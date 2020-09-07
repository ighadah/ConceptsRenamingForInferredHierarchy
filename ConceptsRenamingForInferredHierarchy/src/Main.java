import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;

/**
Author: ghadahalghamdi
*/


public class Main {
	
	
	public void use_rename(String filePath) throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {
		
		OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();
	
		Map<OWLObjectSomeValuesFrom, Map<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>>> obsv_owlclasses_map = new HashMap<>();
		File file1 = new File(filePath);
		IRI iri1 = IRI.create(file1);
		OWLOntology O = manager1.loadOntologyFromOntologyDocument(new IRIDocumentSource(iri1),
				new OWLOntologyLoaderConfiguration().setLoadAnnotationAxioms(true));
		
		System.out.println("the O axioms size: " + O.getLogicalAxiomCount());
		System.out.println("the O classes size: " + O.getClassesInSignature().size());
		System.out.println("the O properties size: " + O.getObjectPropertiesInSignature().size());
		
		OWLOntology O_copy = manager1.createOntology();
		OWLDataFactory df = manager1.getOWLDataFactory();
		Set<OWLEquivalentClassesAxiom> new_equiv_axioms = new HashSet<>();
		Set<OWLSubClassOfAxiom> new_sub_of_axioms = new HashSet<>();
		
		Rename re = new Rename();
		
		for(OWLAxiom ax: O.getAxioms()) {
			if(ax.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
				OWLEquivalentClassesAxiom equiv_ax = (OWLEquivalentClassesAxiom) ax;
				//System.out.println("the current equiv_ax: " + equiv_ax);
				Set<OWLSubClassOfAxiom> subof_ax = equiv_ax.asOWLSubClassOfAxioms();
				for(OWLSubClassOfAxiom subof : subof_ax) {
					if(!subof.isGCI()) {
						OWLClassExpression lhs = subof.getSubClass();
						OWLClassExpression rhs = subof.getSuperClass();
					
						Set<OWLClassExpression> lhs_new_conjunct_set = new HashSet<>();
						if(rhs instanceof OWLObjectIntersectionOf) {
							//System.out.println("the rhs of equiv is conjunct set");
							Set<OWLClassExpression> rhs_conjuncts = rhs.asConjunctSet();
							for(OWLClassExpression rhs_conjunct : rhs_conjuncts) {
								if(rhs_conjunct instanceof OWLObjectSomeValuesFrom) {
									OWLObjectSomeValuesFrom obsv = (OWLObjectSomeValuesFrom) rhs_conjunct;
									
									Map<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>> g_s_classes = re.from_obsv_to_owlclass(obsv);
									//System.out.println("the resulting map after using re: " + g_s_classes);
									
									
									lhs_new_conjunct_set.addAll(g_s_classes.keySet());
									
									
									
									Set<OWLClassExpression> g_conjunct_set = new HashSet<>();
						
									OWLClass g_class = null;
									for(Map.Entry<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>> g_s_class: g_s_classes.entrySet()) {
										g_class = g_s_class.getKey();
	
										Map<OWLObjectSomeValuesFrom, OWLClass> obsv_s_class = g_s_class.getValue();
										for(Map.Entry<OWLObjectSomeValuesFrom, OWLClass> obsv_s_class_entry: obsv_s_class.entrySet()) {
											OWLObjectSomeValuesFrom pv = obsv_s_class_entry.getKey();
											OWLClass s_class = obsv_s_class_entry.getValue();
											g_conjunct_set.add(s_class);
											OWLEquivalentClassesAxiom s_pv_equiv = df.getOWLEquivalentClassesAxiom(s_class, pv);
											//System.out.println("s_pv_equiv: "+ s_pv_equiv);
											manager1.addAxiom(O_copy, s_pv_equiv);
										}
									}
									
									obsv_owlclasses_map.put(obsv, g_s_classes);
									if(g_conjunct_set.size() > 1) {
										OWLObjectIntersectionOf g_conjuncts = df.getOWLObjectIntersectionOf(g_conjunct_set);
										OWLEquivalentClassesAxiom owl_g_equiv = df.getOWLEquivalentClassesAxiom(g_class, g_conjuncts);
										//System.out.println("owl_g_equiv: where s_classes is more than 1 "+ owl_g_equiv);
										manager1.addAxiom(O_copy, owl_g_equiv);
									}else if(g_conjunct_set.size() == 1) {
										for(OWLClassExpression conjunct: g_conjunct_set) {
											OWLEquivalentClassesAxiom owl_g_equiv = df.getOWLEquivalentClassesAxiom(g_class, conjunct);
											//System.out.println("owl_g_equiv: where s_classes equals 1 "+ owl_g_equiv);
											manager1.addAxiom(O_copy, owl_g_equiv);
										}
									}
								}if(rhs_conjunct instanceof OWLClass) {
									lhs_new_conjunct_set.add(rhs_conjunct);
								}
							}
						}
						//A == B
						if(rhs instanceof OWLClass) {
							//System.out.println("the rhs of equiv is owlclass");
							lhs_new_conjunct_set.add(rhs);
						}
						
						//A <= r some B
						if(rhs instanceof OWLObjectSomeValuesFrom) {
							//System.out.println("the rhs of equiv is obsv");
							//////
							//lhs_new_conjunct_set.add(rhs);
							OWLObjectSomeValuesFrom obsv = (OWLObjectSomeValuesFrom) rhs;
							Map<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>> g_s_classes = re.from_obsv_to_owlclass(obsv);
							lhs_new_conjunct_set.addAll(g_s_classes.keySet());
							Set<OWLClassExpression> g_conjunct_set = new HashSet<>();
							OWLClass g_class = null;
							for(Map.Entry<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>> g_s_class: g_s_classes.entrySet()) {
								g_class = g_s_class.getKey();
								Map<OWLObjectSomeValuesFrom, OWLClass> obsv_s_class = g_s_class.getValue();
								for(Map.Entry<OWLObjectSomeValuesFrom, OWLClass> obsv_s_class_entry: obsv_s_class.entrySet()) {
									OWLObjectSomeValuesFrom pv = obsv_s_class_entry.getKey();
									OWLClass s_class = obsv_s_class_entry.getValue();
									g_conjunct_set.add(s_class);
									OWLEquivalentClassesAxiom s_pv_equiv = df.getOWLEquivalentClassesAxiom(s_class, pv);
									manager1.addAxiom(O_copy, s_pv_equiv);
									
								}
							}
							if(g_conjunct_set.size() > 1) {
								OWLObjectIntersectionOf g_conjuncts = df.getOWLObjectIntersectionOf(g_conjunct_set);
								OWLEquivalentClassesAxiom owl_g_equiv = df.getOWLEquivalentClassesAxiom(g_class, g_conjuncts);
								manager1.addAxiom(O_copy, owl_g_equiv);
							}else if(g_conjunct_set.size() == 1) {
								for(OWLClassExpression conjunct: g_conjunct_set) {
									OWLEquivalentClassesAxiom owl_g_equiv = df.getOWLEquivalentClassesAxiom(g_class, conjunct);
									manager1.addAxiom(O_copy, owl_g_equiv);
								}
							}
							
							obsv_owlclasses_map.put(obsv, g_s_classes);

							
						}
						
						OWLObjectIntersectionOf owl_conjuncts = df.getOWLObjectIntersectionOf(lhs_new_conjunct_set);
						OWLEquivalentClassesAxiom owl_equiv = df.getOWLEquivalentClassesAxiom(lhs.asOWLClass(), owl_conjuncts);
						new_equiv_axioms.add(owl_equiv);
						manager1.addAxiom(O_copy, owl_equiv);
					}
				}
			}
			if(ax.isOfType(AxiomType.SUBCLASS_OF)) {
				OWLSubClassOfAxiom subof = (OWLSubClassOfAxiom) ax;
				if(!subof.isGCI()) {
					OWLClassExpression lhs = subof.getSubClass();
					OWLClassExpression rhs = subof.getSuperClass();
					Set<OWLClassExpression> lhs_new_conjunct_set = new HashSet<>();
					//A <= C
					if(rhs instanceof OWLObjectIntersectionOf) {
						//System.out.println("the rhs of subof is conjunct set");
						Set<OWLClassExpression> rhs_conjuncts = rhs.asConjunctSet();
						
						
						for(OWLClassExpression rhs_conjunct : rhs_conjuncts) {
							if(rhs_conjunct instanceof OWLObjectSomeValuesFrom) {
								OWLObjectSomeValuesFrom obsv = (OWLObjectSomeValuesFrom) rhs_conjunct;
								Map<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>> g_s_classes = re.from_obsv_to_owlclass(obsv);
								lhs_new_conjunct_set.addAll(g_s_classes.keySet());
								Set<OWLClassExpression> g_conjunct_set = new HashSet<>();
								OWLClass g_class = null;
								for(Map.Entry<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>> g_s_class: g_s_classes.entrySet()) {
									g_class = g_s_class.getKey();
									Map<OWLObjectSomeValuesFrom, OWLClass> obsv_s_class = g_s_class.getValue();
									for(Map.Entry<OWLObjectSomeValuesFrom, OWLClass> obsv_s_class_entry: obsv_s_class.entrySet()) {
										OWLObjectSomeValuesFrom pv = obsv_s_class_entry.getKey();
										OWLClass s_class = obsv_s_class_entry.getValue();
										g_conjunct_set.add(s_class);
										OWLEquivalentClassesAxiom s_pv_equiv = df.getOWLEquivalentClassesAxiom(s_class, pv);
										manager1.addAxiom(O_copy, s_pv_equiv);
									}
								}
								
								obsv_owlclasses_map.put(obsv, g_s_classes);
								if(g_conjunct_set.size() > 1) {
									OWLObjectIntersectionOf g_conjuncts = df.getOWLObjectIntersectionOf(g_conjunct_set);
									OWLEquivalentClassesAxiom owl_g_equiv = df.getOWLEquivalentClassesAxiom(g_class, g_conjuncts);
									manager1.addAxiom(O_copy, owl_g_equiv);
								}else if(g_conjunct_set.size() == 1) {
									for(OWLClassExpression conjunct: g_conjunct_set) {
										OWLEquivalentClassesAxiom owl_g_equiv = df.getOWLEquivalentClassesAxiom(g_class, conjunct);
										manager1.addAxiom(O_copy, owl_g_equiv);
									}
								}
									
							}if(rhs_conjunct instanceof OWLClass) {
								lhs_new_conjunct_set.add(rhs_conjunct);
							}
						}
							
					}
					//A <= B
					if(rhs instanceof OWLClass) {
						lhs_new_conjunct_set.add(rhs);
					}
					//A <= r some B
					if(rhs instanceof OWLObjectSomeValuesFrom) {
						//System.out.println("the rhs of subof is obsv: " + subof);
						
						OWLObjectSomeValuesFrom obsv = (OWLObjectSomeValuesFrom) rhs;
						Map<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>> g_s_classes = re.from_obsv_to_owlclass(obsv);
						lhs_new_conjunct_set.addAll(g_s_classes.keySet());
						//System.out.println("the map g_s_classes: " + g_s_classes);
						Set<OWLClassExpression> g_conjunct_set = new HashSet<>();
						OWLClass g_class = null;
						for(Map.Entry<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>> g_s_class: g_s_classes.entrySet()) {
							g_class = g_s_class.getKey();
							Map<OWLObjectSomeValuesFrom, OWLClass> obsv_s_class = g_s_class.getValue();
							for(Map.Entry<OWLObjectSomeValuesFrom, OWLClass> obsv_s_class_entry: obsv_s_class.entrySet()) {
								OWLObjectSomeValuesFrom pv = obsv_s_class_entry.getKey();
								OWLClass s_class = obsv_s_class_entry.getValue();
								g_conjunct_set.add(s_class);
								OWLEquivalentClassesAxiom s_pv_equiv = df.getOWLEquivalentClassesAxiom(s_class, pv);
								manager1.addAxiom(O_copy, s_pv_equiv);
								
							}
						}
						if(g_conjunct_set.size() > 1) {
							OWLObjectIntersectionOf g_conjuncts = df.getOWLObjectIntersectionOf(g_conjunct_set);
							OWLEquivalentClassesAxiom owl_g_equiv = df.getOWLEquivalentClassesAxiom(g_class, g_conjuncts);
							manager1.addAxiom(O_copy, owl_g_equiv);
						}else if(g_conjunct_set.size() == 1) {
							for(OWLClassExpression conjunct: g_conjunct_set) {
								OWLEquivalentClassesAxiom owl_g_equiv = df.getOWLEquivalentClassesAxiom(g_class, conjunct);
								manager1.addAxiom(O_copy, owl_g_equiv);
							}
						}
						obsv_owlclasses_map.put(obsv, g_s_classes);

						
					}
					OWLObjectIntersectionOf owl_conjuncts = df.getOWLObjectIntersectionOf(lhs_new_conjunct_set);
					OWLSubClassOfAxiom owl_subof = df.getOWLSubClassOfAxiom(lhs, owl_conjuncts);
					
					new_sub_of_axioms.add(owl_subof);
					manager1.addAxiom(O_copy, owl_subof);
				}
				
			}
			
		}
		
		
		System.out.println("O_copy axioms: " + O_copy.getAxioms());
		OutputStream os_1 = new FileOutputStream(filePath + "_copy-2.owl");
		manager1.saveOntology(O_copy, new FunctionalSyntaxDocumentFormat(), os_1);
		//classify O_copy
		OWLOntology O_copy_classified = classifyOntology(O_copy);
		
		System.out.println("O_copy_classified axioms: " + O_copy_classified.getAxioms());
		OutputStream os_2 = new FileOutputStream(filePath + "_copy_classified-2.owl");
		manager1.saveOntology(O_copy_classified, new FunctionalSyntaxDocumentFormat(), os_2);
		
		OWLOntology O_classified = manager1.createOntology();
		Set<OWLSubClassOfAxiom> subofs_with_original_names = new HashSet<>();

		for(OWLAxiom axiom: O_copy_classified.getAxioms()) {
			if(axiom.isOfType(AxiomType.SUBCLASS_OF)) {
				OWLSubClassOfAxiom axiom_subof = (OWLSubClassOfAxiom) axiom;
				if(!axiom_subof.isGCI()) {
					OWLClassExpression lhs = axiom_subof.getSubClass();
					OWLClassExpression rhs = axiom_subof.getSuperClass();
					if((!lhs.toString().contains("PVRG_")) && (!rhs.toString().contains("PVRG_"))
							&& (!rhs.toString().contains("PV_")) && (!lhs.toString().contains("PV_")) && (!lhs.toString().contains("PVS_")) && (!rhs.toString().contains("PVS_"))) {
						manager1.addAxiom(O_classified, axiom_subof);
					}
						OWLClass rhs_cl = (OWLClass) rhs;
						//System.out.println("the current: " + axiom_subof);
						if(rhs_cl.toString().contains("PVRG_") && !lhs.toString().contains("PVRG_")) {
							//System.out.println("the axiom_subof with rhs as PVRG_ and lhs as owlclass is: " + axiom_subof);
							System.out.println("obsv_owlclasses_map: " + obsv_owlclasses_map);
							for(Map.Entry<OWLObjectSomeValuesFrom, Map<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>>> obsv_owlclasses_entry : obsv_owlclasses_map.entrySet()) {
								OWLObjectSomeValuesFrom obsv_owlclasses_entry_key = obsv_owlclasses_entry.getKey();
								Map<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>> obsv_owlclasses_entry_values = obsv_owlclasses_entry.getValue();
								for(Map.Entry<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>> g_s_classes_entry : obsv_owlclasses_entry_values.entrySet()) {
									OWLClass g_s_classes_entry_key = g_s_classes_entry.getKey();
									System.out.println("g_s_classes_entry_key: "+ g_s_classes_entry_key);
									System.out.println("rhs_cl: " + rhs_cl);
									if(g_s_classes_entry_key.toString().equals(rhs_cl.toString())) {
										OWLSubClassOfAxiom subof_ax_original_name = df.getOWLSubClassOfAxiom(lhs, obsv_owlclasses_entry_key);
										//System.out.println("the subof_ax_original_name: " + subof_ax_original_name);
										subofs_with_original_names.add(subof_ax_original_name);
										manager1.addAxiom(O_classified, subof_ax_original_name);
									}
								}
							}
						} 
						if(rhs_cl.toString().contains("PVS_") && !lhs.toString().contains("PVRG_")) {
							//System.out.println("the axiom_subof with rhs as PVRG_ and lhs as owlclass is: " + axiom_subof);
							System.out.println("obsv_owlclasses_map: " + obsv_owlclasses_map);
							for(Map.Entry<OWLObjectSomeValuesFrom, Map<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>>> obsv_owlclasses_entry : obsv_owlclasses_map.entrySet()) {
								OWLObjectSomeValuesFrom obsv_owlclasses_entry_key = obsv_owlclasses_entry.getKey();
								Map<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>> obsv_owlclasses_entry_values = obsv_owlclasses_entry.getValue();
								for(Map.Entry<OWLClass, Map<OWLObjectSomeValuesFrom, OWLClass>> g_s_classes_entry : obsv_owlclasses_entry_values.entrySet()) {
									OWLClass g_s_classes_entry_key = g_s_classes_entry.getKey();
									System.out.println("g_s_classes_entry_key: "+ g_s_classes_entry_key);
									System.out.println("rhs_cl: " + rhs_cl);
									if(g_s_classes_entry_key.toString().equals(rhs_cl.toString())) {
										OWLSubClassOfAxiom subof_ax_original_name = df.getOWLSubClassOfAxiom(lhs, obsv_owlclasses_entry_key);
										System.out.println("the subof_ax_original_name: " + subof_ax_original_name);
										subofs_with_original_names.add(subof_ax_original_name);
										manager1.addAxiom(O_classified, subof_ax_original_name);
									}
								}
							}
						}
				}
			}
		}
		
		
		
		System.out.println("O_classified axioms: " + O_classified.getAxioms());
		
		OutputStream os_3 = new FileOutputStream(filePath + "_classified-2.owl");
		manager1.saveOntology(O_classified, new FunctionalSyntaxDocumentFormat(), os_3);
		
	}
	
	
	
public OWLOntology classifyOntology(OWLOntology ontology) throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = manager.getOWLDataFactory();
		OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
		
		
		System.out.println("ontology classes size = " + ontology.getClassesInSignature().size());
		System.out.println("ontology properties size = " + ontology.getObjectPropertiesInSignature().size());
		
		
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
		
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
		gens.add(new InferredSubClassAxiomGenerator());
		gens.add(new InferredEquivalentClassAxiomGenerator());

		OWLOntology infOnt = manager.createOntology();
		InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner,
				gens);
		iog.fillOntology(df, infOnt);
		reasoner.dispose();
		return infOnt;			
	}
	public static void main(String args[]) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException, ClassNotFoundException {
	
		Main g = new Main();
		
		//String filePath= "/Users/ghadahalghamdi/Downloads/MRI_of_lower_limb_simple_example.owl";
		String filePath = "/Users/ghadahalghamdi/Documents/testing_examples/testing-renaming-approach/ex-2-transitive-property.owl";
		//String filePath = "/Users/ghadahalghamdi/Documents/testing_examples/testing-renaming-approach/ex-1-o.owl";
		g.use_rename(filePath);
		  
	}

}


