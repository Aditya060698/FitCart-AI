from fastapi import APIRouter

from app.schemas.vectors import VectorIngestionRequest, VectorIngestionResponse
from app.services.vector_ingestion_service import ingest_documents


router = APIRouter(prefix="/vectors", tags=["Vectors"])


@router.post("/ingest", response_model=VectorIngestionResponse)
def ingest_vectors_route(request: VectorIngestionRequest) -> VectorIngestionResponse:
    return ingest_documents(request)
