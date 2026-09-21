import json
from pathlib import Path
from typing import Dict, List, Optional

from pydantic import BaseModel


class DecodedTerm(BaseModel):
    id: str
    term: str
    one_liner: str
    plain_explanation: str
    what_to_ask_doctor: str
    source_id: str


class DrugProduct(BaseModel):
    brand_name: str
    generic_name: str
    therapeutic_class: str
    common_forms: List[str]
    source_id: str


class KnowledgeSource(BaseModel):
    source_id: str
    source_name: str
    license: str
    retrieval_date: str
    purpose: str


class KnowledgeService:
    """Ingests and queries open medical knowledge bases per NN-3.

    Every clinical fact returned cites a verified source_id registered in knowledge/SOURCES.md.
    """

    def __init__(self, knowledge_dir: Optional[Path] = None):
        if knowledge_dir is None:
            # Locate repo root knowledge dir
            repo_root = Path(__file__).resolve().parents[3]
            self.knowledge_dir = repo_root / "knowledge"
        else:
            self.knowledge_dir = knowledge_dir

        self.terms: Dict[str, DecodedTerm] = {}
        self.drugs: List[DrugProduct] = []
        self.sources: Dict[str, KnowledgeSource] = {}
        self._load_knowledge()

    def _load_knowledge(self) -> None:
        # Load sources from SOURCES.md
        sources_path = self.knowledge_dir / "SOURCES.md"
        if sources_path.exists():
            content = sources_path.read_text(encoding="utf-8")
            table_lines = [
                line.strip()
                for line in content.splitlines()
                if line.strip().startswith("| `src_")
            ]
            for line in table_lines:
                parts = [p.strip() for p in line.split("|")[1:-1]]
                if len(parts) >= 5:
                    source_id = parts[0].strip("`")
                    self.sources[source_id] = KnowledgeSource(
                        source_id=source_id,
                        source_name=parts[1],
                        license=parts[2],
                        retrieval_date=parts[3],
                        purpose=parts[4],
                    )

        # Load glossary seed terms
        terms_path = self.knowledge_dir / "glossary" / "seed_terms.json"
        if terms_path.exists():
            with open(terms_path, "r", encoding="utf-8") as f:
                terms_raw = json.load(f)
                for item in terms_raw:
                    term_obj = DecodedTerm(**item)
                    self.terms[term_obj.term.lower()] = term_obj

        # Load brand/generic seeds
        brands_path = self.knowledge_dir / "brand_generic" / "seed_brands.json"
        if brands_path.exists():
            with open(brands_path, "r", encoding="utf-8") as f:
                brands_raw = json.load(f)
                for item in brands_raw:
                    self.drugs.append(DrugProduct(**item))

    def get_term(self, term: str) -> Optional[DecodedTerm]:
        cleaned = term.strip().lower()
        if cleaned in self.terms:
            return self.terms[cleaned]
        # Partial / word boundary match
        for key, val in self.terms.items():
            if key in cleaned or cleaned in key:
                return val
        return None

    def search_terms(self, query: str) -> List[DecodedTerm]:
        q = query.strip().lower()
        return [
            term
            for term in self.terms.values()
            if q in term.term.lower() or q in term.plain_explanation.lower()
        ]

    def search_drugs(self, query: str) -> List[DrugProduct]:
        q = query.strip().lower()
        results = []
        for drug in self.drugs:
            if (
                q in drug.brand_name.lower()
                or q in drug.generic_name.lower()
                or q in drug.therapeutic_class.lower()
            ):
                results.append(drug)
        return results

    def get_drug_by_brand(self, brand: str) -> Optional[DrugProduct]:
        b = brand.strip().lower()
        for drug in self.drugs:
            if drug.brand_name.lower() == b:
                return drug
        return None

    def get_registered_sources(self) -> List[KnowledgeSource]:
        return list(self.sources.values())

    def is_valid_source(self, source_id: str) -> bool:
        return source_id in self.sources or source_id.startswith("obs_")


# Global service singleton
knowledge_service = KnowledgeService()
