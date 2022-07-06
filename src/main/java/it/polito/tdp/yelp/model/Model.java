package it.polito.tdp.yelp.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.yelp.db.YelpDao;

public class Model {
	private YelpDao dao;
	private Graph<Review, DefaultWeightedEdge> grafo;
	private List<Review> vertici;
	private List <Business> tendina;
	private List<Review> best;
	private double maxGiorni;



	public Model() {
		this.dao= new YelpDao();
	}

	public List<String> popolaCmbCity(){
		return dao.getCities();
	}

	public List<Business> listaCmbBusiness(String city){
		this.tendina= new ArrayList<Business>();
		for(Business bi: this.dao.popolaBusiness(city)) {
			tendina.add(bi);
		}
		return tendina;
	}


	public void creaGrafo(Business b) {

		this.grafo= new SimpleDirectedWeightedGraph<Review, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		this.vertici=new ArrayList<Review>(this.dao.getVertici(b));

		Graphs.addAllVertices(this.grafo, vertici);

		for(Review r1: this.grafo.vertexSet()) {
			for(Review r2: this.grafo.vertexSet() ) {
				if(!r1.equals(r2)) { 
					double peso= ChronoUnit.DAYS.between(r1.getDate(), r2.getDate());
					double pesoOff= Math.abs(peso);
					if(pesoOff!=0) {
						if(r1.getDate().isAfter(r2.getDate())){	
							Graphs.addEdgeWithVertices(this.grafo, r2, r1, pesoOff);
						}else if(r2.getDate().isAfter(r1.getDate())) {
							Graphs.addEdgeWithVertices(this.grafo, r1, r2, pesoOff);
						}
					}
				}
			}
		}


	}

	public String nVertici() {
		return "Grafo creato!"+"\n"+"#verici: "+ this.grafo.vertexSet().size()+"\n";
	}

	public String nArchi() {
		return "#archi: "+ this.grafo.edgeSet().size()+"\n";
	}
	
	public String archiMax() {
		String s="";
		int max=0;
		List<Review> list= new LinkedList<Review>();
		
		for(Review ri: this.vertici) {
			if(this.grafo.outgoingEdgesOf(ri).size()>max) {
				max=this.grafo.edgesOf(ri).size();
			}
		}
		
		for(Review r: this.vertici) {
			if(this.grafo.outgoingEdgesOf(r).size()==max) {
				list.add(r);
			}
			
		}
		for(Review r: list) {
			s+=r+ "#Archi uscenti: "+max+"\n";
		}
		return s;
	}
	
	public String miglioramento(){
		List<Review> parziale= new ArrayList<>();
		String s="";
		for(Review r: this.vertici) {
			parziale.add(r);
			cerca(parziale, r);
		}
		this.best= new ArrayList<Review>();
		LocalDate prima=parziale.get(0).getDate();
		LocalDate ultimo= parziale.get(parziale.size()-1).getDate();
		
		this.maxGiorni= ultimo.getDayOfYear()-prima.getDayOfYear();
		
		for(Review ri: best) {
			s+= ri.getReviewId()+"\n";
		}
		return s+" "+this.maxGiorni;
	}

	private void cerca(List<Review> parziale, Review partenza) {
		if(best!=null) {
		if(parziale.size()>best.size()) {
			this.best= new ArrayList<Review>(parziale);
			return;
		}
		}
		for(Review ri: this.vertici) {
			if(!parziale.contains(ri)) {
				if(ri.getStars()-parziale.get(parziale.size()-1).getStars()>=0){
					parziale.add(ri);
					cerca(parziale, ri);
					parziale.remove(parziale.size()-1);
					
				}
			}
		}
		
	}
	
	
}
